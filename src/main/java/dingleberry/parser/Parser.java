package dingleberry.parser;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dingleberry.command.AddCommand;
import dingleberry.command.Command;
import dingleberry.command.DeleteCommand;
import dingleberry.command.ExitCommand;
import dingleberry.command.FindCommand;
import dingleberry.command.ListCommand;
import dingleberry.command.MarkCommand;
import dingleberry.command.UnmarkCommand;
import dingleberry.exception.DingleberryException;
import dingleberry.model.Deadlines;
import dingleberry.model.Events;
import dingleberry.model.Todo;

/**
 * Makes sense of a raw line of user input: identifies the {@link CommandWord},
 * validates its parameters, and turns the whole line into a ready-to-execute
 * {@link Command}, throwing {@link DingleberryException} when the input
 * doesn't match what the command expects.
 */
public final class Parser {
    /** Defines the accepted date/time format for deadline and event input. */
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
    /** Matches an optional relative qualifier and a weekday name. */
    private static final Pattern WEEKDAY_PATTERN = Pattern.compile(
            "(?i)(?:(this|next)\\s+)?"
                    + "(mon(?:day)?|tue(?:sday)?|wed(?:nesday)?|"
                    + "thu(?:rsday)?|fri(?:day)?|sat(?:urday)?|"
                    + "sun(?:day)?)(?:\\s+(.+))?");
    /** Matches a twelve-hour time with an optional colon or dot separator. */
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?i)(\\d{1,2})(?:(?::|\\.)(\\d{2}))?\\s*(am|pm)");
    /** Defines the number of characters used to identify a weekday. */
    private static final int WEEKDAY_PREFIX_LENGTH = 3;
    /** Defines the number of days in a week. */
    private static final int DAYS_IN_WEEK = 7;
    /** Defines the number of hours in a twelve-hour clock half. */
    private static final int HOURS_PER_HALF_DAY = 12;
    /** Defines the largest valid minute value. */
    private static final int MAX_MINUTE = 59;
    /** Defines the zero offset used for same-day relative dates. */
    private static final int ZERO_OFFSET = 0;
    /** Defines the first valid hour on a twelve-hour clock. */
    private static final int FIRST_HOUR = 1;
    /** Defines the capture group containing a twelve-hour time suffix. */
    private static final int TIME_SUFFIX_GROUP = 3;
    /** Defines the separator between a command word and its arguments. */
    private static final String COMMAND_SEPARATOR = " ";
    /** Defines the literal prefix used by deadline commands. */
    private static final String DEADLINE_PREFIX = " /by ";
    /** Defines the literal prefix used by event start times. */
    private static final String EVENT_FROM_PREFIX = " /from ";
    /** Defines the literal prefix used by event end times. */
    private static final String EVENT_TO_PREFIX = " /to ";
    /** Defines the example date/time text shown in user-facing errors. */
    private static final String DATE_TIME_EXAMPLE = "2019-12-02 1800";

    private Parser() {
    }

    /**
     * Parses a full line of user input into an executable {@link Command}.
     *
     * @param fullCommand the raw user input to interpret
     * @return the validated command object ready to execute
     * @throws DingleberryException if the input is blank, unknown, or invalid.
     */
    public static Command parse(final String fullCommand)
            throws DingleberryException {
        if (fullCommand.isBlank()) {
            throw new DingleberryException(
                    "Please give me a command or a task description.");
        }

        CommandWord commandWord = CommandWord.fromInput(fullCommand);
        if (commandWord == null) {
            throw new DingleberryException(
                    "I don't recognize that command. Use 'todo', 'list',"
                            + " 'mark', 'unmark', 'delete', 'deadline',"
                            + " or 'event'.",
                    DingleberryException.ErrorType.WRONG_COMMAND);
        }
        assert commandWord != null
                : "A parsed command must have a command word.";

        switch (commandWord) {
        case BYE:
            return new ExitCommand();
        case LIST:
            requireNoParameters(commandWord, fullCommand);
            return new ListCommand();
        case FIND:
            return new FindCommand(parseKeyword(commandWord, fullCommand));
        case DELETE:
            return new DeleteCommand(parseTaskNumber(commandWord, fullCommand));
        case MARK:
            return new MarkCommand(parseTaskNumber(commandWord, fullCommand));
        case UNMARK:
            return new UnmarkCommand(parseTaskNumber(commandWord, fullCommand));
        case TODO:
            return new AddCommand(parseTodo(commandWord, fullCommand));
        case DEADLINE:
            return new AddCommand(parseDeadline(commandWord, fullCommand));
        case EVENT:
            return new AddCommand(parseEvent(commandWord, fullCommand));
        default:
            // Unreachable: every CommandWord value is handled above.
            assert false : "Every command word must have a parser branch.";
            throw new DingleberryException("Unhandled command: " + commandWord);
        }
    }

    /**
     * Parses the keyword following a "find" command.
     *
     * @param command the find command being parsed.
     * @param input the full raw input line.
     * @return the keyword following the command.
     * @throws DingleberryException if the keyword is blank.
     */
    private static String parseKeyword(final CommandWord command,
                                      final String input)
            throws DingleberryException {
        String keyword = extractArgument(command, input);
        if (keyword.isBlank()) {
            throw new DingleberryException(
                    "'" + command.keyword() + "' needs a keyword.");
        }
        return keyword;
    }

    /**
     * Rejects extra text after a command that takes no parameters, e.g.
     * "list".
     *
     * @param command the command being validated
     * @param input the full raw input line
     * @throws DingleberryException if the command has unexpected parameters.
     */
    private static void requireNoParameters(final CommandWord command,
                                            final String input)
            throws DingleberryException {
        if (!command.isExactInput(input)) {
            throw new DingleberryException(
                    "'" + command.keyword() + "' does not accept parameters.");
        }
    }

    /**
     * Parses the one-based task number following a command such as "delete",
     * "mark", or "unmark".
     *
     * @param command the command being parsed
     * @param input the full raw input line
     * @return the one-based index supplied by the user
     * @throws DingleberryException if the task number is missing or not numeric
     */
    private static int parseTaskNumber(final CommandWord command,
                                      final String input)
            throws DingleberryException {
        String taskNumberText = extractArgument(command, input);
        try {
            return Integer.parseInt(taskNumberText);
        } catch (NumberFormatException e) {
            throw new DingleberryException(
                    "'" + command.keyword() + "' needs a task number.");
        }
    }

    /**
     * Parses a "todo &lt;description&gt;" command into a {@link Todo}.
     *
     * @param command the todo command being parsed.
     * @param input the full raw input line.
     * @return the parsed todo.
     * @throws DingleberryException if the description is blank.
     */
    private static Todo parseTodo(final CommandWord command,
                                  final String input)
            throws DingleberryException {
        String description = requireValue(
                extractArgument(command, input), command.keyword());
        return new Todo(description);
    }

    /**
     * Parses a deadline command into a {@link Deadlines} task.
     *
     * @param command the deadline command being parsed.
     * @param input the full raw input line.
     * @return the parsed deadline.
     * @throws DingleberryException if the command has invalid details.
     */
    private static Deadlines parseDeadline(final CommandWord command,
                                          final String input)
            throws DingleberryException {
        int byIndex = input.toLowerCase().indexOf(DEADLINE_PREFIX);
        if (byIndex <= command.keyword().length()) {
            throw new DingleberryException(
                    "A deadline needs a description and '/by <date>' in the"
                            + " format yyyy-MM-dd HHmm, e.g. "
                            + DATE_TIME_EXAMPLE + ".");
        }

        String description = requireValue(
                input.substring(command.keyword().length()
                        + COMMAND_SEPARATOR.length(), byIndex),
                command.keyword());
        String dueDateText = requireValue(
                input.substring(byIndex + DEADLINE_PREFIX.length()),
                command.keyword());
        return new Deadlines(description,
            parseDateTime(dueDateText, LocalDate.now(), null));
    }

    /**
     * Parses an event command into an {@link Events} task.
     *
     * @param command the event command being parsed.
     * @param input the full raw input line.
     * @return the parsed event.
     * @throws DingleberryException if the command has invalid details.
     */
    private static Events parseEvent(final CommandWord command,
                                    final String input)
            throws DingleberryException {
        String lowerCaseInput = input.toLowerCase();
        int fromIndex = lowerCaseInput.indexOf(EVENT_FROM_PREFIX);
        int toIndex = lowerCaseInput.indexOf(EVENT_TO_PREFIX);
        if (fromIndex <= command.keyword().length() || toIndex <= fromIndex) {
            throw new DingleberryException(
                    "An event needs a description, '/from <time>',"
                            + " and '/to <time>' in the format"
                            + " yyyy-MM-dd HHmm, e.g. " + DATE_TIME_EXAMPLE
                            + ".");
        }

        String description = requireValue(
                input.substring(command.keyword().length()
                        + COMMAND_SEPARATOR.length(), fromIndex),
                command.keyword());
        String fromText = requireValue(
                input.substring(
                        fromIndex + EVENT_FROM_PREFIX.length(), toIndex),
                command.keyword());
        String toText = requireValue(
                input.substring(toIndex + EVENT_TO_PREFIX.length()),
                command.keyword());
        LocalDate today = LocalDate.now();
        LocalDateTime fromDateTime = parseDateTime(fromText, today, null);
        LocalDateTime toDateTime = parseDateTime(toText, today,
            fromDateTime.toLocalDate());
        return new Events(description, fromDateTime, toDateTime);
    }

    /**
     * Returns the trimmed text after a command keyword, or an empty string when
     * no value follows the command.
     *
     * @param command the command to strip
     * @param input the full raw input line
     * @return the remaining argument text after the command keyword
     */
    private static String extractArgument(final CommandWord command,
                                         final String input) {
        String commandText = command.keyword() + COMMAND_SEPARATOR;
        int startIndex = commandText.length();
        if (input.length() <= command.keyword().length()) {
            return "";
        }
        return input.substring(startIndex).trim();
    }

    /**
     * Validates that a required text field is present and non-blank.
     *
     * @param value the candidate value to validate
     * @param command the command name used in the error message
     * @return the trimmed, non-empty value
     * @throws DingleberryException if the value is blank
     */
    private static String requireValue(final String value, final String command)
            throws DingleberryException {
        String trimmedValue = value.trim();
        if (trimmedValue.isBlank()) {
            throw new DingleberryException(
                    "The " + command + " needs a non-empty description"
                            + " and details.");
        }
        return trimmedValue;
    }

    /**
     * Parses an absolute or natural date/time into a {@link LocalDateTime}.
     *
     * @param text the raw timestamp string to parse
     * @param referenceDate the date used to resolve relative weekdays
     * @param inheritedDate the date used for a time-only event endpoint
     * @return the parsed local date and time
     * @throws DingleberryException if the supplied text is not in the expected
     *     format.
     */
    static LocalDateTime parseDateTime(final String text,
                                       final LocalDate referenceDate,
                                       final LocalDate inheritedDate)
            throws DingleberryException {
        String trimmedText = text.trim();
        try {
            return LocalDateTime.parse(trimmedText, INPUT_DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            // Continue with the natural formats after trying the legacy one.
        }

        Matcher weekdayMatcher = WEEKDAY_PATTERN.matcher(trimmedText);
        if (weekdayMatcher.matches()) {
            LocalDate weekdayDate = resolveWeekday(
                    weekdayMatcher.group(1), weekdayMatcher.group(2),
                    referenceDate);
                LocalTime time = parseOptionalTime(
                    weekdayMatcher.group(TIME_SUFFIX_GROUP));
            return LocalDateTime.of(weekdayDate, time);
        }

        Matcher timeMatcher = TIME_PATTERN.matcher(trimmedText);
        if (timeMatcher.matches() && inheritedDate != null) {
            return LocalDateTime.of(inheritedDate, parseTime(timeMatcher));
        }

        throw invalidDateTimeException();
    }

    /**
     * Resolves a weekday qualifier against the supplied reference date.
     *
     * @param qualifier the optional relative qualifier
     * @param weekdayText the weekday name to resolve
     * @param referenceDate the date from which to resolve the weekday
     * @return the resolved weekday date
     * @throws DingleberryException if the weekday cannot be resolved.
     */
    private static LocalDate resolveWeekday(final String qualifier,
                                            final String weekdayText,
                                            final LocalDate referenceDate)
            throws DingleberryException {
        DayOfWeek targetDay = weekdayOf(weekdayText);
        int daysUntil = targetDay.getValue() - referenceDate.getDayOfWeek()
                .getValue();
        if (daysUntil < ZERO_OFFSET
            || (daysUntil == ZERO_OFFSET
            && !"this".equalsIgnoreCase(qualifier))) {
            daysUntil += DAYS_IN_WEEK;
        }
        if ("next".equalsIgnoreCase(qualifier)
            && daysUntil == ZERO_OFFSET) {
            daysUntil = DAYS_IN_WEEK;
        }
        return referenceDate.plusDays(daysUntil);
    }

    /**
     * Converts a weekday name into its corresponding {@link DayOfWeek}.
     *
     * @param weekdayText the weekday name to convert
     * @return the corresponding day of the week
     * @throws DingleberryException if the weekday cannot be converted.
     */
    private static DayOfWeek weekdayOf(final String weekdayText)
            throws DingleberryException {
        String normalized = weekdayText.toLowerCase(Locale.ROOT);
        return switch (normalized.substring(0, WEEKDAY_PREFIX_LENGTH)) {
        case "mon" -> DayOfWeek.MONDAY;
        case "tue" -> DayOfWeek.TUESDAY;
        case "wed" -> DayOfWeek.WEDNESDAY;
        case "thu" -> DayOfWeek.THURSDAY;
        case "fri" -> DayOfWeek.FRIDAY;
        case "sat" -> DayOfWeek.SATURDAY;
        case "sun" -> DayOfWeek.SUNDAY;
        default -> throw invalidDateTimeException();
        };
    }

    /**
     * Returns midnight or parses the optional time following a weekday.
     *
     * @param timeText the optional time text
     * @return the parsed time, or midnight when no time is supplied
     * @throws DingleberryException if the time cannot be parsed.
     */
    private static LocalTime parseOptionalTime(final String timeText)
            throws DingleberryException {
        if (timeText == null) {
            return LocalTime.MIDNIGHT;
        }
        Matcher matcher = TIME_PATTERN.matcher(timeText.trim());
        if (!matcher.matches()) {
            throw invalidDateTimeException();
        }
        return parseTime(matcher);
    }

    /**
     * Converts a matched twelve-hour time into a {@link LocalTime}.
     *
     * @param matcher the regular-expression match containing the time
     * @return the parsed local time
     * @throws DingleberryException if the time is outside the valid range.
     */
    private static LocalTime parseTime(final Matcher matcher)
            throws DingleberryException {
        int hour = Integer.parseInt(matcher.group(1));
        int minute = matcher.group(2) == null
            ? ZERO_OFFSET : Integer.parseInt(matcher.group(2));
        if (hour < FIRST_HOUR || hour > HOURS_PER_HALF_DAY
                || minute > MAX_MINUTE) {
            throw invalidDateTimeException();
        }
        if ("pm".equalsIgnoreCase(matcher.group(TIME_SUFFIX_GROUP))
                && hour != HOURS_PER_HALF_DAY) {
            hour += HOURS_PER_HALF_DAY;
        } else if ("am".equalsIgnoreCase(matcher.group(TIME_SUFFIX_GROUP))
                && hour == HOURS_PER_HALF_DAY) {
            hour = ZERO_OFFSET;
        }
        return LocalTime.of(hour, minute);
    }

    /**
     * Creates the standard parser error for an unsupported date/time.
     *
     * @return the standard invalid-date exception
     */
    private static DingleberryException invalidDateTimeException() {
        return new DingleberryException(
                "I couldn't understand that date/time. Use yyyy-MM-dd HHmm,"
                        + " a weekday such as Mon or next Monday, or a time"
                        + " such as 2:30pm.");
    }
}
