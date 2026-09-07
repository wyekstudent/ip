package dingleberry.command;

import dingleberry.exception.DingleberryException;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Unmarks the task at a one-based index parsed from user input. */
public class UnmarkCommand extends Command {
    /** Stores the one-based task index to unmark. */
    private final int taskNumber;

    /**
     * Creates a command that unmarks the given one-based task number.
     *
     * @param taskIndex the one-based index of the task to unmark.
     */
    public UnmarkCommand(final int taskIndex) {
        this.taskNumber = taskIndex;
    }

    @Override
    public final void execute(final TaskList tasks, final Ui ui,
                              final Storage storage)
            throws DingleberryException {
        // Bounds depend on the current task list, so check them at execution.
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new DingleberryException(
                    "That task number is not in the list.");
        }
        assert taskNumber - 1 >= 0 && taskNumber - 1 < tasks.size()
            : "A validated task number must map to a valid list index.";
        final Task taskToUnmark = tasks.get(taskNumber - 1);
        assert taskToUnmark != null
            : "A task list must not contain null tasks.";
        taskToUnmark.unmarkDone();
        ui.showTaskUnmarked(taskToUnmark);
        saveTasks(tasks, storage, ui);
    }
}
