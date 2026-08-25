import java.io.IOException;

public class Dingleberry {
    private static final String DATA_FILE_PATH = "./data/dingleberry.txt";

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
                    ui.showTaskList(dinglelist);
                    break;
                case DELETE:
                    int taskNumber = Parser.parseTaskNumber(command, input);
                    if (taskNumber < 1 || taskNumber > dinglelist.size()) {
                        throw new DingleberryException("That task number is not in the list.");
                    }
                    Task deletedTask = dinglelist.remove(taskNumber - 1);
                    ui.showTaskDeleted(deletedTask, dinglelist.size());
                    saveTasks(storage, dinglelist, ui);
                    break;
                case TODO:
                    dinglelist.add(Parser.parseTodo(command, input));
                    ui.showTaskAdded(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist, ui);
                    break;
                case DEADLINE:
                    dinglelist.add(Parser.parseDeadline(command, input));
                    ui.showTaskAdded(dinglelist.get(dinglelist.size() - 1), dinglelist.size());
                    saveTasks(storage, dinglelist, ui);
                    break;
                case EVENT:
                    dinglelist.add(Parser.parseEvent(command, input));
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

    private static void saveTasks(Storage storage, TaskList dinglelist, Ui ui) {
        try {
            storage.save(dinglelist);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }
}

