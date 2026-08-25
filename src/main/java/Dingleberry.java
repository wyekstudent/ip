import java.io.IOException;

/**
 * Entry point and orchestrator for the Dingleberry task-tracking chatbot.
 * Wires together the {@link Ui}, {@link Storage}, and {@link TaskList}
 * collaborators and drives the read-parse-execute loop in {@link #run()}.
 */
public class Dingleberry {
    private final Ui ui;
    private final Storage storage;
    private TaskList tasks;

    public Dingleberry(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            tasks = new TaskList(storage.load());
        } catch (IOException e) {
            ui.showLoadingError(e.getMessage());
            tasks = new TaskList();
        }
    }

    /** Runs the welcome-loop-goodbye lifecycle until the user closes input or issues "bye". */
    public void run() {
        ui.showWelcome();

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            try {
                Command command = Parser.parse(input);
                command.execute(tasks, ui, storage);
                if (command.isExit()) {
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

    public static void main(String[] args) {
        new Dingleberry("./data/dingleberry.txt").run();
    }
}


