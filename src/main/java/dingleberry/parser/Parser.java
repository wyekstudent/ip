package dingleberry.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    /** Finds parameter markers as complete input tokens. */
    private static final Pattern PARAMETER_PATTERN = Pattern.compile(
            "(?i)/(by|from|to)(?=\\s|$)");
    /** Defines the separator between a command word and its arguments. */
    private static final String COMMAND_SEPARATOR = " ";
    /** Defines the example date/time text shown in user-facing errors. */
    private static final String DATE_TIME_EXAMPLE = "2019-12-02 1800";
    /** Defines the example relative weekday shown in user-facing errors. */
    private static final String RELATIVE_DATE_EXAMPLE = "next Tue";

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
        if (fullCommand == null) {
            throw new DingleberryException(
                    "Please give me a command or a task description.");
        }
        String input = normalizeInput(fullCommand);
        if (input.isBlank()) {
            throw new DingleberryException(
                    "Please give me a command or a task description.");
        }

        CommandWord commandWord = CommandWord.fromInput(input);
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
            requireNoParameters(commandWord, input);
            return new ListCommand();
        case FIND:
            return new FindCommand(parseKeyword(commandWord, input));
        case DELETE:
            return new DeleteCommand(parseTaskNumber(commandWord, input));
        case MARK:
            return new MarkCommand(parseTaskNumber(commandWord, input));
        case UNMARK:
            return new UnmarkCommand(parseTaskNumber(commandWord, input));
        case TODO:
            return new AddCommand(parseTodo(commandWord, input));
        case DEADLINE:
            return new AddCommand(parseDeadline(commandWord, input));
        case EVENT:
            return new AddCommand(parseEvent(commandWord, input));
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
        if (taskNumberText.isBlank()) {
            throw new DingleberryException(
                    "'" + command.keyword() + "' needs a task number.");
        }
        try {
            int taskNumber = Integer.parseInt(taskNumberText);
            if (taskNumber < 1) {
                throw new DingleberryException(
                        "'" + command.keyword()
                                + "' needs a positive task number.");
            }
            return taskNumber;
        } catch (NumberFormatException e) {
            throw new DingleberryException(
                    "'" + command.keyword()
                            + "' needs a numeric task number.");
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
        Matcher parameterMatcher = PARAMETER_PATTERN.matcher(input);
        if (!parameterMatcher.find()
            || !"by".equalsIgnoreCase(parameterMatcher.group(1))
            || parameterMatcher.find()) {
            throw new DingleberryException(
                    "A deadline needs a description and '/by <date>' in the"
                            + " format yyyy-MM-dd HHmm, e.g. "
                        + DATE_TIME_EXAMPLE + ", or a weekday such as "
                        + RELATIVE_DATE_EXAMPLE + ".");
        }
            Matcher byMatcher = PARAMETER_PATTERN.matcher(input);
            byMatcher.find();
            int byIndex = byMatcher.start();

        String description = requireValue(
                input.substring(command.keyword().length() + 1, byIndex),
                command.keyword());
        String dueDateText = requireValue(
                input.substring(byMatcher.end()),
                command.keyword());
        return new Deadlines(description,
                DateTimeParser.parse(dueDateText, LocalDate.now(), null));
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
        Matcher parameterMatcher = PARAMETER_PATTERN.matcher(input);
        if (!parameterMatcher.find()) {
            throw new DingleberryException(
                    "An event needs a description, '/from <time>',"
                            + " and '/to <time>' in the format"
                        + " yyyy-MM-dd HHmm, e.g. " + DATE_TIME_EXAMPLE
                        + ", or a weekday such as "
                        + RELATIVE_DATE_EXAMPLE + ".");
        }
        int fromIndex = parameterMatcher.start();
        String fromMarker = parameterMatcher.group(1).toLowerCase(Locale.ROOT);
        if (!"from".equals(fromMarker)) {
            if ("to".equals(fromMarker)) {
            throw new DingleberryException(
                "An event needs '/from <time>' before '/to <time>'.");
            }
            throw new DingleberryException(
                "An event needs a description, '/from <time>',"
                    + " and '/to <time>' in the format"
                    + " yyyy-MM-dd HHmm, e.g. " + DATE_TIME_EXAMPLE
                    + ", or a weekday such as "
                    + RELATIVE_DATE_EXAMPLE + ".");
        }
        if (!parameterMatcher.find()) {
            throw new DingleberryException(
                        "An event needs a description, '/from <time>',"
                            + " and '/to <time>' in the format"
                            + " yyyy-MM-dd HHmm, e.g. " + DATE_TIME_EXAMPLE
                            + ", or a weekday such as "
                            + RELATIVE_DATE_EXAMPLE + ".");
            }
            int toIndex = parameterMatcher.start();
            if (!"to".equalsIgnoreCase(parameterMatcher.group(1))
                    || parameterMatcher.find()) {
                    throw new DingleberryException(
                        "An event needs '/from <time>' before '/to <time>'.");
            }

        String description = requireValue(
                input.substring(command.keyword().length() + 1, fromIndex),
                command.keyword());
        String fromText = requireValue(
                input.substring(input.indexOf(' ', fromIndex) + 1, toIndex),
                command.keyword());
        String toText = requireValue(
                input.substring(input.indexOf(' ', toIndex) + 1),
                command.keyword());
        LocalDate today = LocalDate.now();
        LocalDateTime fromDateTime = DateTimeParser.parse(
                fromText, today, null);
        LocalDateTime toDateTime = DateTimeParser.parse(toText, today,
                fromDateTime.toLocalDate());
        if (!toDateTime.isAfter(fromDateTime)) {
            throw new DingleberryException(
                    "An event's end time must be after its start time.");
        }
        return new Events(description, fromDateTime, toDateTime);
    }

    /**
     * Returns input with harmless surrounding and repeated whitespace removed.
     *
     * @param input the raw user input.
     * @return the normalized input.
     */
    private static String normalizeInput(final String input) {
        return input.trim().replaceAll("\\s+", " ");
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

}
