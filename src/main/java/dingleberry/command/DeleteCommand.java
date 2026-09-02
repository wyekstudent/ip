package dingleberry.command;

import dingleberry.exception.DingleberryException;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Deletes the task at a one-based index parsed from user input. */
public class DeleteCommand extends Command {
    /** Stores the one-based task index to delete. */
    private final int taskNumber;

    /**
     * Creates a command that deletes the given one-based task number.
     *
     * @param taskIndex the one-based index of the task to delete.
     */
    public DeleteCommand(final int taskIndex) {
        this.taskNumber = taskIndex;
    }

    @Override
    public final void execute(final TaskList tasks, final Ui ui,
                              final Storage storage)
            throws DingleberryException {
        // Bounds depend on the current task list.
        // Check them during execution.
        // time.
        if (taskNumber < 1 || taskNumber > tasks.size()) {
                    throw new DingleberryException(
                        "That task number is not in the list.");
        }
        final Task deletedTask = tasks.remove(taskNumber - 1);
        ui.showTaskDeleted(deletedTask, tasks.size());
        saveTasks(tasks, storage, ui);
    }
}
