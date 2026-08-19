import java.util.ArrayList;
import java.util.Scanner;

public class Dingleberry {
    private static final String SEPARATOR = "____________________________________________________________";

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
        ArrayList<Task> dinglelist = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            try {
                if (input.isBlank()) {
                    throw new DingleberryException("Please give me a command or a task description.");
                } else if (input.equalsIgnoreCase("bye")) {
                    break;
                } else if (input.equalsIgnoreCase("list") || input.toLowerCase().startsWith("list ")) {
                    if (!input.equalsIgnoreCase("list")) {
                        throw new DingleberryException("'list' does not accept parameters.");
                    }
                    System.out.println(SEPARATOR);
                    for (int i = 0; i < dinglelist.size(); i++) {
                        System.out.printf("%d.%s\n", i + 1, dinglelist.get(i));
                    }
                    System.out.println(SEPARATOR);
                } else if (input.equalsIgnoreCase("delete") || input.toLowerCase().startsWith("delete ")) {
                    String taskNumberText = input.length() > 6 ? input.substring(7).trim() : "";
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
                } else if (input.equalsIgnoreCase("todo") || input.toLowerCase().startsWith("todo ")) {
                    String description = requireValue(input.length() > 4 ? input.substring(5) : "", "todo");
                    dinglelist.add(new ToDos(description));
                    printAddedTask(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                } else if (input.equalsIgnoreCase("deadline") || input.toLowerCase().startsWith("deadline ")) {
                    int byIndex = input.toLowerCase().indexOf(" /by ");
                    if (byIndex <= 9) {
                        throw new DingleberryException("A deadline needs a description and '/by <date>'.");
                    }
                    String description = requireValue(input.substring(9, byIndex), "deadline");
                    String dueDate = requireValue(input.substring(byIndex + 5), "deadline");
                    dinglelist.add(new Deadlines(description, dueDate));
                    printAddedTask(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                } else if (input.equalsIgnoreCase("event") || input.toLowerCase().startsWith("event ")) {
                    int fromIndex = input.toLowerCase().indexOf(" /from ");
                    int toIndex = input.toLowerCase().indexOf(" /to ");
                    if (fromIndex <= 6 || toIndex <= fromIndex) {
                        throw new DingleberryException("An event needs a description, '/from <time>', and '/to <time>'.");
                    }
                    String description = requireValue(input.substring(6, fromIndex), "event");
                    String from = requireValue(input.substring(fromIndex + 7, toIndex), "event");
                    String to = requireValue(input.substring(toIndex + 5), "event");
                    dinglelist.add(new Events(description, from, to));
                    printAddedTask(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                } else {
                    throw new DingleberryException(
                            "I don't recognize that command. Use 'todo', 'list', 'delete', 'deadline', or 'event'.",
                            DingleberryException.ErrorType.WRONG_COMMAND);
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

    private static void printAddedTask(Task task, int count) {
        System.out.println(SEPARATOR);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + count + " tasks in the list.");
        System.out.println(SEPARATOR);
    }
}
