package dingleberry.command;

import dingleberry.exception.DingleberryException;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Deletes the task at a one-based index parsed from user input. */
public class DeleteCommand extends Command {
    private final int taskNumber;

    /** Creates a command that deletes the given one-based task number. */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws DingleberryException {
        // Bounds depend on the current task list, so this can only be checked at execution time.
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new DingleberryException("That task number is not in the list.");
        }
        Task deletedTask = tasks.remove(taskNumber - 1);
        ui.showTaskDeleted(deletedTask, tasks.size());
        saveTasks(tasks, storage, ui);
    }
}
