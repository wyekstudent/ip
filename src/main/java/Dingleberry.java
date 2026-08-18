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
        System.out.println(banner);

        Scanner scanner = new Scanner(System.in);

        while(true) {
            String input = scanner.nextLine();

            // Make it case insensitive

            if (input.equalsIgnoreCase("bye")) {
                break;
            }
            System.out.println("____________________________________________________________");
            System.out.println(input);
            System.out.println("____________________________________________________________");
        }
        scanner.close();

        System.out.println("Bya hope to see your berries again!");
    }
}
