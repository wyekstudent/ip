package dingleberry.command;

import dingleberry.exception.DingleberryException;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Deletes the task at a one-based index parsed from user input. */
public class DeleteCommand extends Command {
    private final int taskNumber;

    /**
     * Creates a command that deletes the task identified by the user-facing one-based number.
     *
     * @param taskNumber the one-based task index supplied by the user
     */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Deletes the selected task after validating that the index is still in range.
     *
     * @param tasks the task list to modify
     * @param ui the UI used to print the confirmation message
     * @param storage the storage layer used to persist the updated list
     * @throws DingleberryException if the task number is outside the current list bounds
     */
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
