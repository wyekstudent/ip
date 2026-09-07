package dingleberry.command;

import dingleberry.exception.DingleberryException;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Marks the task at a one-based index parsed from user input as done. */
public class MarkCommand extends Command {
    /** Stores the one-based task index to mark. */
    private final int taskNumber;

    /**
     * Creates a command that marks the given one-based task number as done.
     *
     * @param taskIndex the one-based index of the task to mark.
     */
    public MarkCommand(final int taskIndex) {
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
        final Task taskToMark = tasks.get(taskNumber - 1);
        assert taskToMark != null : "A task list must not contain null tasks.";
        taskToMark.markAsDone();
        ui.showTaskMarked(taskToMark);
        saveTasks(tasks, storage, ui);
    }
}
