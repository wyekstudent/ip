import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Dingleberry {
    private static final String DATA_FILE_PATH = "./data/dingleberry.txt";
    // Expected user input format for date/times, e.g. "2019-12-02 1800".
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        Storage storage = new Storage(DATA_FILE_PATH);
        TaskList dinglelist;
        try {
            dinglelist = new TaskList(storage.load());
        } catch (IOException e) {
            ui.showLoadingError(e.getMessage());
            dinglelist = new TaskList();
        }

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            try {
                if (input.isBlank()) {
                    throw new DingleberryException("Please give me a command or a task description.");
                }

                Command command = Command.fromInput(input);
                if (command == null) {
                    throw new DingleberryException(
                            "I don't recognize that command. Use 'todo', 'list', 'delete', 'deadline', or 'event'.",
                            DingleberryException.ErrorType.WRONG_COMMAND);
                }

                switch (command) {
                case BYE:
                    break;
                case LIST:
                    if (!command.isExactInput(input)) {
                        throw new DingleberryException("'list' does not accept parameters.");
                    }
                    ui.showTaskList(dinglelist);
                    break;
                case DELETE:
                    String taskNumberText = input.length() > command.keyword().length()
                            ? input.substring(command.keyword().length() + 1).trim() : "";
                    int taskNumber;
                    try {
                        taskNumber = Integer.parseInt(taskNumberText);
                    } catch (NumberFormatException e) {
                        throw new DingleberryException("'delete' needs a task number.");
                    }
                    if (taskNumber < 1 || taskNumber > dinglelist.size()) {
                        throw new DingleberryException("That task number is not in the list.");
                    }
                    Task deletedTask = dinglelist.remove(taskNumber - 1);
                    ui.showTaskDeleted(deletedTask, dinglelist.size());
                    saveTasks(storage, dinglelist, ui);
                    break;
                case TODO:
                    String description = requireValue(input.length() > command.keyword().length()
                            ? input.substring(command.keyword().length() + 1) : "", command.keyword());
                    dinglelist.add(new ToDos(description));
                    ui.showTaskAdded(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist, ui);
                    break;
                case DEADLINE:
                    int byIndex = input.toLowerCase().indexOf(" /by ");
                    if (byIndex <= command.keyword().length()) {
                        throw new DingleberryException("A deadline needs a description and '/by <date>'.");
                    }
                    String deadlineDescription = requireValue(input.substring(command.keyword().length() + 1, byIndex), command.keyword());
                    String dueDateText = requireValue(input.substring(byIndex + 5), command.keyword());
                    LocalDateTime dueDate = parseDateTime(dueDateText);
                    dinglelist.add(new Deadlines(deadlineDescription, dueDate));
                    ui.showTaskAdded(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist, ui);
                    break;
                case EVENT:
                    int fromIndex = input.toLowerCase().indexOf(" /from ");
                    int toIndex = input.toLowerCase().indexOf(" /to ");
                    if (fromIndex <= command.keyword().length() || toIndex <= fromIndex) {
                        throw new DingleberryException("An event needs a description, '/from <time>', and '/to <time>'.");
                    }
                    String eventDescription = requireValue(input.substring(command.keyword().length() + 1, fromIndex), command.keyword());
                    String fromText = requireValue(input.substring(fromIndex + 7, toIndex), command.keyword());
                    String toText = requireValue(input.substring(toIndex + 5), command.keyword());
                    LocalDateTime from = parseDateTime(fromText);
                    LocalDateTime to = parseDateTime(toText);
                    dinglelist.add(new Events(eventDescription, from, to));
                    ui.showTaskAdded(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist, ui);
                    break;
                }
            } catch (DingleberryException e) {
                if (e.isWrongCommand()) {
                    ui.showWrongCommandError(e.getMessage());
                } else {
                    ui.showIncorrectParametersError(e.getMessage());
                }
            }
        }
        ui.close();

        ui.showGoodbye();
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

    private static void saveTasks(Storage storage, TaskList dinglelist, Ui ui) {
        try {
            storage.save(dinglelist);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }
}
