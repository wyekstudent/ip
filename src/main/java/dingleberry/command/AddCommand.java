package dingleberry.command;

import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Adds an already-constructed task (todo, deadline, or event) to the list. */
public class AddCommand extends Command {
    private final Task taskToAdd;

    /**
     * Creates a command that adds the given task when executed.
     *
     * @param taskToAdd the task instance to append to the list
     */
    public AddCommand(Task taskToAdd) {
        this.taskToAdd = taskToAdd;
    }

    /**
     * Adds the task, prints the resulting confirmation, and saves the updated list.
     *
     * @param tasks the task list to modify
     * @param ui the UI used to report the change
     * @param storage the storage layer used to persist the updated list
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        tasks.add(taskToAdd);
        ui.showTaskAdded(taskToAdd, tasks.size());
        saveTasks(tasks, storage, ui);
    }
}
