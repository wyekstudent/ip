package dingleberry.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        return new Deadlines(description, parseDateTime(dueDateText));
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
                    "An event needs a description, '/from <time>', and"
                            + " '/to <time>' in the format yyyy-MM-dd HHmm,"
                            + " e.g. " + DATE_TIME_EXAMPLE + ".");
        }

        String description = requireValue(
                input.substring(command.keyword().length()
                        + COMMAND_SEPARATOR.length(), fromIndex),
                command.keyword());
        String fromText = requireValue(
                input.substring(fromIndex + EVENT_FROM_PREFIX.length(), toIndex),
                command.keyword());
        String toText = requireValue(
                input.substring(toIndex + EVENT_TO_PREFIX.length()),
                command.keyword());
        return new Events(description, parseDateTime(fromText),
                parseDateTime(toText));
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
                    "The " + command + " needs a non-empty description and"
                            + " details.");
        }
        return trimmedValue;
    }

    /**
     * Parses a date/time given by the user (expected format "yyyy-MM-dd HHmm",
     * e.g. "2019-12-02 1800") into a {@link LocalDateTime}.
     *
     * @param text the raw timestamp string to parse
     * @return the parsed local date and time
     * @throws DingleberryException if the supplied text is not in the expected
     *     format.
     */
    private static LocalDateTime parseDateTime(final String text)
            throws DingleberryException {
        try {
            return LocalDateTime.parse(text.trim(), INPUT_DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new DingleberryException(
                    "I couldn't understand that date/time. Please use the"
                            + " format yyyy-MM-dd HHmm, e.g. "
                            + DATE_TIME_EXAMPLE + ".");
        }
    }
}
