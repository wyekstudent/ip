import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Makes sense of a raw line of user input for a given {@link Command}:
 * validates its parameters and turns them into ready-to-use values (task
 * numbers or constructed {@link Task}s), throwing {@link DingleberryException}
 * when the input doesn't match what the command expects.
 */
public class Parser {
    // Expected user input format for date/times, e.g. "2019-12-02 1800".
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /** Identifies the command keyword at the start of the input, or null if none matches. */
    public static Command parseCommand(String input) {
        return Command.fromInput(input);
    }

    /** Rejects any extra text after a command that takes no parameters, e.g. "list". */
    public static void requireNoParameters(Command command, String input) throws DingleberryException {
        if (!command.isExactInput(input)) {
            throw new DingleberryException("'" + command.keyword() + "' does not accept parameters.");
        }
    }

    /** Parses the one-based task number following a "delete" command. */
    public static int parseTaskNumber(Command command, String input) throws DingleberryException {
        String taskNumberText = input.length() > command.keyword().length()
                ? input.substring(command.keyword().length() + 1).trim() : "";
        try {
            return Integer.parseInt(taskNumberText);
        } catch (NumberFormatException e) {
            throw new DingleberryException("'" + command.keyword() + "' needs a task number.");
        }
    }

    /** Parses a "todo &lt;description&gt;" command into a {@link ToDos}. */
    public static ToDos parseTodo(Command command, String input) throws DingleberryException {
        String description = requireValue(input.length() > command.keyword().length()
                ? input.substring(command.keyword().length() + 1) : "", command.keyword());
        return new ToDos(description);
    }

    /** Parses a "deadline &lt;description&gt; /by &lt;date&gt;" command into a {@link Deadlines}. */
    public static Deadlines parseDeadline(Command command, String input) throws DingleberryException {
        int byIndex = input.toLowerCase().indexOf(" /by ");
        if (byIndex <= command.keyword().length()) {
            throw new DingleberryException("A deadline needs a description and '/by <date>'.");
        }
        String description = requireValue(input.substring(command.keyword().length() + 1, byIndex), command.keyword());
        String dueDateText = requireValue(input.substring(byIndex + 5), command.keyword());
        return new Deadlines(description, parseDateTime(dueDateText));
    }

    /** Parses an "event &lt;description&gt; /from &lt;time&gt; /to &lt;time&gt;" command into an {@link Events}. */
    public static Events parseEvent(Command command, String input) throws DingleberryException {
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
