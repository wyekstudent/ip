import java.util.Scanner;

public class Dingleberry {
    private static final int MAX_TASKS = 100;
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
        int count = 0;
        Task[] dinglelist = new Task[MAX_TASKS];

        while(count < MAX_TASKS) {
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("bye")) {
                break;
            } else if (input.equalsIgnoreCase("list")) {
                System.out.println(SEPARATOR);
                for (int i = 1; i <= count; i++) {
                    System.out.printf("%d.%s\n", i, dinglelist[i - 1]);
                }
                System.out.println(SEPARATOR);
            } else if (input.toLowerCase().startsWith("mark ")) {
                try {
                    int taskNumber = Integer.parseInt(input.substring(5)) - 1;
                    if (taskNumber >= 0 && taskNumber < count) {
                        dinglelist[taskNumber].markAsDone();
                        System.out.println(SEPARATOR);
                        System.out.println("Nice! I've marked this task as done");
                        System.out.println(dinglelist[taskNumber]);
                        System.out.println(SEPARATOR);
                    } else {
                        System.out.println("Invalid task number!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid format! Use 'mark <item no.>'");
                }
            } else if (input.toLowerCase().startsWith("unmark ")) {
                try {
                    int taskNumber = Integer.parseInt(input.substring(7)) - 1;
                    if (taskNumber >= 0 && taskNumber < count) {
                        dinglelist[taskNumber].unmarkDone();
                        System.out.println(SEPARATOR);
                        System.out.println("OK, I've marked this task as not done yet");
                        System.out.println(dinglelist[taskNumber]);
                        System.out.println(SEPARATOR);
                    } else {
                        System.out.println("Invalid task number!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid format! Use 'unmark <item no.>'");
                }
            } else if (input.toLowerCase().startsWith("todo ")) {
                dinglelist[count++] = new ToDos(input.substring(5));
                System.out.println(SEPARATOR);
                System.out.println("Got it. I've added this task:");
                System.out.println("  " + dinglelist[count - 1]);
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(SEPARATOR);
            } else if (input.toLowerCase().startsWith("deadline ")) {
                int byIndex = input.toLowerCase().indexOf(" /by ");
                if (byIndex > 9) {
                    dinglelist[count++] = new Deadlines(input.substring(9, byIndex), input.substring(byIndex + 5));
                    System.out.println(SEPARATOR);
                    System.out.println("Got it. I've added this task:");
                    System.out.println("  " + dinglelist[count - 1]);
                    System.out.println("Now you have " + count + " tasks in the list.");
                    System.out.println(SEPARATOR);
                }
            } else if (input.toLowerCase().startsWith("event ")) {
                int fromIndex = input.toLowerCase().indexOf(" /from ");
                int toIndex = input.toLowerCase().indexOf(" /to ");
                if (fromIndex > 6 && toIndex > fromIndex) {
                    dinglelist[count++] = new Events(input.substring(6, fromIndex),
                            input.substring(fromIndex + 7, toIndex), input.substring(toIndex + 5));
                    System.out.println(SEPARATOR);
                    System.out.println("Got it. I've added this task:");
                    System.out.println("  " + dinglelist[count - 1]);
                    System.out.println("Now you have " + count + " tasks in the list.");
                    System.out.println(SEPARATOR);
                }
            } else {
                dinglelist[count++] = new ToDos(input);
                System.out.println(SEPARATOR);
                System.out.println("added: " + dinglelist[count - 1]);
                System.out.println(SEPARATOR);
            }
        }
        scanner.close();

        System.out.println("Bya hope to see your berries again!");
    }
}
