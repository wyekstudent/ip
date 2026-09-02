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
    // Expected user input format for date/times, e.g. "2019-12-02 1800".
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
    /** Defines the length of the separator after a command keyword. */
    private static final int PREFIX_SEPARATOR_LENGTH = 1;
    /** Defines the length of the deadline's by-prefix. */
    private static final int BY_PREFIX_LENGTH = 5;
    /** Defines the length of the event's from-prefix. */
    private static final int FROM_PREFIX_LENGTH = 7;

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
                            + " 'delete', 'deadline', or 'event'.",
                    DingleberryException.ErrorType.WRONG_COMMAND);
        }

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
        case TODO:
            return new AddCommand(parseTodo(commandWord, fullCommand));
        case DEADLINE:
            return new AddCommand(parseDeadline(commandWord, fullCommand));
        case EVENT:
            return new AddCommand(parseEvent(commandWord, fullCommand));
        default:
            // Unreachable: every CommandWord value is handled above.
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
        String keyword = input.length() > command.keyword().length()
                ? input.substring(command.keyword().length()
                    + PREFIX_SEPARATOR_LENGTH).trim() : "";
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
     * Parses the one-based task number following a "delete" command.
     *
     * @param command the delete command being parsed
     * @param input the full raw input line
     * @return the one-based index supplied by the user
     * @throws DingleberryException if the task number is missing or not numeric
     */
    private static int parseTaskNumber(final CommandWord command,
                                       final String input)
            throws DingleberryException {
        String taskNumberText = input.length() > command.keyword().length()
                ? input.substring(command.keyword().length()
                    + PREFIX_SEPARATOR_LENGTH).trim() : "";
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
        final String description = requireValue(
            input.length() > command.keyword().length()
                ? input.substring(command.keyword().length()
                + PREFIX_SEPARATOR_LENGTH) : "", command.keyword());
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
        int byIndex = input.toLowerCase().indexOf(" /by ");
        if (byIndex <= command.keyword().length()) {
            throw new DingleberryException(
                    "A deadline needs a description and '/by <date>'.");
        }
        final String description = requireValue(
                input.substring(command.keyword().length()
                    + PREFIX_SEPARATOR_LENGTH, byIndex),
                command.keyword());
        final String dueDateText = requireValue(
            input.substring(byIndex + BY_PREFIX_LENGTH), command.keyword());
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
        int fromIndex = input.toLowerCase().indexOf(" /from ");
        int toIndex = input.toLowerCase().indexOf(" /to ");
        if (fromIndex <= command.keyword().length() || toIndex <= fromIndex) {
                    throw new DingleberryException(
                        "An event needs a description, '/from <time>', and"
                            + " '/to <time>'.");
        }
        final String description = requireValue(
                input.substring(command.keyword().length()
                    + PREFIX_SEPARATOR_LENGTH, fromIndex),
                command.keyword());
        final String fromText = requireValue(
                input.substring(fromIndex + FROM_PREFIX_LENGTH, toIndex),
                command.keyword());
        final String toText = requireValue(
            input.substring(toIndex + BY_PREFIX_LENGTH), command.keyword());
        return new Events(description, parseDateTime(fromText),
            parseDateTime(toText));
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
        if (value.isBlank()) {
                throw new DingleberryException(
                    "The " + command + " needs a non-empty description and"
                        + " details.");
        }
        return value;
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
                            + " format yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.");
        }
    }
}
