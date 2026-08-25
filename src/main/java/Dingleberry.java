import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Dingleberry {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String DATA_FILE_PATH = "./data/dingleberry.txt";
    // Expected user input format for date/times, e.g. "2019-12-02 1800".
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    public static void main(String[] args) {
        String banner =
                """
                        ____________________________________________________________
                         ____  _             _      _                          \s
                        |  _ \\(_)_ __   __ _| | ___| |__   ___ _ __ _ __ _   _\s
                        | | | | | '_ \\ / _` | |/ _ \\ '_ \\ / _ \\ '__| '__| | | |
                        | |_| | | | | | (_| | |  __/ |_) |  __/ |  | |  | |_| |
                        |____/|_|_| |_|\\__, |_|\\___|_.__/ \\___|_|  |_|   \\__, |
                                       |___/                              |___|
                        
                        Hey There! I'm Dingleberry
                        What can I do for you?
                        ____________________________________________________________
                        """;
        System.out.println(banner);

        Scanner scanner = new Scanner(System.in);
        Storage storage = new Storage(DATA_FILE_PATH);
        ArrayList<Task> dinglelist;
        try {
            dinglelist = storage.load();
        } catch (IOException e) {
            System.out.println(SEPARATOR);
            System.out.println("Couldn't load saved tasks (" + e.getMessage() + "). Starting with an empty list.");
            System.out.println(SEPARATOR);
            dinglelist = new ArrayList<>();
        }

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

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
                    System.out.println(SEPARATOR);
                    for (int i = 0; i < dinglelist.size(); i++) {
                        System.out.printf("%d.%s\n", i + 1, dinglelist.get(i));
                    }
                    System.out.println(SEPARATOR);
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
                    System.out.println(SEPARATOR);
                    System.out.println("Noted. I've removed this task: " + deletedTask);
                    System.out.println("Now you have " + dinglelist.size() + " tasks in the list");
                    System.out.println(SEPARATOR);
                    saveTasks(storage, dinglelist);
                    break;
                case TODO:
                    String description = requireValue(input.length() > command.keyword().length()
                            ? input.substring(command.keyword().length() + 1) : "", command.keyword());
                    dinglelist.add(new ToDos(description));
                    printAddedTask(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist);
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
                    printAddedTask(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist);
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
                    printAddedTask(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist);
                    break;
                }
            } catch (DingleberryException e) {
                System.out.println(SEPARATOR);
                if (e.isWrongCommand()) {
                    System.out.println("Oops, Dingleberry doesn't know that command: " + e.getMessage());
                } else {
                    System.out.println("Oops, Dingleberry found incorrect parameters: " + e.getMessage());
                }
                System.out.println("Please try again with the correct command and parameters.");
                System.out.println(SEPARATOR);
            }
        }
        scanner.close();

        System.out.println("Bya hope to see your berries again!");
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

    private static void printAddedTask(Task task, int count) {
        System.out.println(SEPARATOR);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + count + " tasks in the list.");
        System.out.println(SEPARATOR);
    }

    private static void saveTasks(Storage storage, ArrayList<Task> dinglelist) {
        try {
            storage.save(dinglelist);
        } catch (IOException e) {
            System.out.println(SEPARATOR);
            System.out.println("Couldn't save tasks to disk (" + e.getMessage() + ").");
            System.out.println(SEPARATOR);
        }
    }
}
