import java.util.Scanner;

public class Dingleberry {
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
                        Whata cana I doa for you?
                        ____________________________________________________________
                        """;
        // Prints out above banner
        System.out.println(banner);

        // Takes in user input
        Scanner scanner = new Scanner(System.in);
        int count = 0;
        int maxTasks = 100; // Max number of tasks
        String[] dinglelist = new String[maxTasks];

        while(count < maxTasks) {
            String input = scanner.nextLine();

            // Make it case-insensitive
            if (input.equalsIgnoreCase("bye")) {
                break;
            } else if (input.equalsIgnoreCase("list")) {
                // Sets the list item
                System.out.println("____________________________________________________________");
                for (int i = 1; i <= count; i++) {
                    System.out.printf("%d. %s\n", i, dinglelist[i - 1]);
                }
                System.out.println("____________________________________________________________");
            } else {
                dinglelist[count++] = input;
                System.out.println("____________________________________________________________");
                System.out.println("added: " + input);
                System.out.println("____________________________________________________________");
            }
        }
        scanner.close();

        System.out.println("Bya hope to see your berries again!");
    }
}
