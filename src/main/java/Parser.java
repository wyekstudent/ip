import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Makes sense of a raw line of user input: identifies the {@link CommandWord},
 * validates its parameters, and turns the whole line into a ready-to-execute
 * {@link Command}, throwing {@link DingleberryException} when the input
 * doesn't match what the command expects.
 */
public class Parser {
    // Expected user input format for date/times, e.g. "2019-12-02 1800".
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /** Parses a full line of user input into an executable {@link Command}. */
    public static Command parse(String fullCommand) throws DingleberryException {
        if (fullCommand.isBlank()) {
            throw new DingleberryException("Please give me a command or a task description.");
        }

        CommandWord commandWord = CommandWord.fromInput(fullCommand);
        if (commandWord == null) {
            throw new DingleberryException(
                    "I don't recognize that command. Use 'todo', 'list', 'delete', 'deadline', or 'event'.",
                    DingleberryException.ErrorType.WRONG_COMMAND);
        }

        switch (commandWord) {
        case BYE:
            return new ExitCommand();
        case LIST:
            requireNoParameters(commandWord, fullCommand);
            return new ListCommand();
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

    /** Rejects any extra text after a command that takes no parameters, e.g. "list". */
    private static void requireNoParameters(CommandWord command, String input) throws DingleberryException {
        if (!command.isExactInput(input)) {
            throw new DingleberryException("'" + command.keyword() + "' does not accept parameters.");
        }
    }

    /** Parses the one-based task number following a "delete" command. */
    private static int parseTaskNumber(CommandWord command, String input) throws DingleberryException {
        String taskNumberText = input.length() > command.keyword().length()
                ? input.substring(command.keyword().length() + 1).trim() : "";
        try {
            return Integer.parseInt(taskNumberText);
        } catch (NumberFormatException e) {
            throw new DingleberryException("'" + command.keyword() + "' needs a task number.");
        }
    }

    /** Parses a "todo &lt;description&gt;" command into a {@link ToDos}. */
    private static ToDos parseTodo(CommandWord command, String input) throws DingleberryException {
        String description = requireValue(input.length() > command.keyword().length()
                ? input.substring(command.keyword().length() + 1) : "", command.keyword());
        return new ToDos(description);
    }

    /** Parses a "deadline &lt;description&gt; /by &lt;date&gt;" command into a {@link Deadlines}. */
    private static Deadlines parseDeadline(CommandWord command, String input) throws DingleberryException {
        int byIndex = input.toLowerCase().indexOf(" /by ");
        if (byIndex <= command.keyword().length()) {
            throw new DingleberryException("A deadline needs a description and '/by <date>'.");
        }
        String description = requireValue(input.substring(command.keyword().length() + 1, byIndex), command.keyword());
        String dueDateText = requireValue(input.substring(byIndex + 5), command.keyword());
        return new Deadlines(description, parseDateTime(dueDateText));
    }

    /** Parses an "event &lt;description&gt; /from &lt;time&gt; /to &lt;time&gt;" command into an {@link Events}. */
    private static Events parseEvent(CommandWord command, String input) throws DingleberryException {
        int fromIndex = input.toLowerCase().indexOf(" /from ");
        int toIndex = input.toLowerCase().indexOf(" /to ");
        if (fromIndex <= command.keyword().length() || toIndex <= fromIndex) {
            throw new DingleberryException("An event needs a description, '/from <time>', and '/to <time>'.");
        }
        String description = requireValue(input.substring(command.keyword().length() + 1, fromIndex), command.keyword());
        String fromText = requireValue(input.substring(fromIndex + 7, toIndex), command.keyword());
        String toText = requireValue(input.substring(toIndex + 5), command.keyword());
        return new Events(description, parseDateTime(fromText), parseDateTime(toText));
    }

    private static String requireValue(String value, String command) throws DingleberryException {
        if (value.isBlank()) {
            throw new DingleberryException("The " + command + " needs a non-empty description and details.");
        }
        return value;
    }

    /**
     * Parses a date/time given by the user (expected format "yyyy-MM-dd HHmm",
     * e.g. "2019-12-02 1800") into a {@link LocalDateTime}.
     */
    private static LocalDateTime parseDateTime(String text) throws DingleberryException {
        try {
            return LocalDateTime.parse(text.trim(), INPUT_DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new DingleberryException(
                    "I couldn't understand that date/time. Please use the format yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.");
        }
    }
}
