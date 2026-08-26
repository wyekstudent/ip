package dingleberry.command;

import java.io.IOException;

import dingleberry.exception.DingleberryException;
import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/**
 * Represents a single user command, already validated and ready to run
 * against the app's collaborators. Produced by {@link dingleberry.parser.Parser#parse}.
 */
public abstract class Command {
    /**
     * Carries out this command's effect on the task list, UI, and storage.
     *
     * @param tasks the in-memory task list to mutate or read
     * @param ui the UI used to print success or error messages
     * @param storage the persistence layer used to save modified task data
     * @throws DingleberryException if the command's execution fails validation or business rules
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws DingleberryException;

    /**
     * Returns whether executing this command should end the program's main loop.
     *
     * @return true if the app should stop reading user input after this command
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Persists the task list, reporting any I/O failure through the UI instead of throwing.
     *
     * @param tasks the task list to save
     * @param storage the storage object responsible for writing data
     * @param ui the UI used to display save errors
     */
    protected void saveTasks(TaskList tasks, Storage storage, Ui ui) {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }
}
