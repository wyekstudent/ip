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

    /** Runs the welcome-loop-goodbye lifecycle until the user closes input. */
    public void run() {
        ui.showWelcome();

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            try {
                if (input.isBlank()) {
                    throw new DingleberryException("Please give me a command or a task description.");
                }

                Command command = Parser.parseCommand(input);
                if (command == null) {
                    throw new DingleberryException(
                            "I don't recognize that command. Use 'todo', 'list', 'delete', 'deadline', or 'event'.",
                            DingleberryException.ErrorType.WRONG_COMMAND);
                }

                switch (command) {
                case BYE:
                    break;
                case LIST:
                    Parser.requireNoParameters(command, input);
                    ui.showTaskList(tasks);
                    break;
                case DELETE:
                    int taskNumber = Parser.parseTaskNumber(command, input);
                    if (taskNumber < 1 || taskNumber > tasks.size()) {
                        throw new DingleberryException("That task number is not in the list.");
                    }
                    Task deletedTask = tasks.remove(taskNumber - 1);
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    saveTasks();
                    break;
                case TODO:
                    tasks.add(Parser.parseTodo(command, input));
                    ui.showTaskAdded(tasks.get(tasks.size() - 1), tasks.size());
                    saveTasks();
                    break;
                case DEADLINE:
                    tasks.add(Parser.parseDeadline(command, input));
                    ui.showTaskAdded(tasks.get(tasks.size() - 1), tasks.size());
                    saveTasks();
                    break;
                case EVENT:
                    tasks.add(Parser.parseEvent(command, input));
                    ui.showTaskAdded(tasks.get(tasks.size() - 1), tasks.size());
                    saveTasks();
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

    private void saveTasks() {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Dingleberry("./data/dingleberry.txt").run();
    }
}

