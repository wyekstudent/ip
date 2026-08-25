package dingleberry.command;

import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Adds an already-constructed task (todo, deadline, or event) to the list. */
public class AddCommand extends Command {
    private final Task taskToAdd;

    public AddCommand(Task taskToAdd) {
        this.taskToAdd = taskToAdd;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        tasks.add(taskToAdd);
        ui.showTaskAdded(taskToAdd, tasks.size());
        saveTasks(tasks, storage, ui);
    }
}
