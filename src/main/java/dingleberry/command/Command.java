package dingleberry.command;

import java.io.IOException;

import dingleberry.exception.DingleberryException;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/**
 * Represents a single user command, already validated and ready to run
 * against the app's collaborators. Produced by {@link Parser#parse}.
 */
public abstract class Command {
    /** Carries out this command's effect on the task list, UI, and storage. */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws DingleberryException;

    /** Whether executing this command should end the program's main loop. */
    public boolean isExit() {
        return false;
    }

    /** Persists the task list, reporting any I/O failure through the UI instead of throwing. */
    protected void saveTasks(TaskList tasks, Storage storage, Ui ui) {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }
}
