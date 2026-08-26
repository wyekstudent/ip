package dingleberry.command;

import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Prints the current task list; carries no parsed data of its own. */
public class ListCommand extends Command {
    /**
     * Displays the current tasks in their numbered user-facing form.
     *
     * @param tasks the tasks to display
     * @param ui the UI used to render the list
     * @param storage the storage layer, unused for this command
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskList(tasks);
    }
}
