package dingleberry.command;

import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Signals that the main loop should stop; the goodbye message is printed by Dingleberry once it does. */
public class ExitCommand extends Command {
    /**
     * Performs no mutation because exiting is represented by the command's exit flag.
     *
     * @param tasks the current task list, unchanged by this command
     * @param ui the UI used for any future output
     * @param storage the persistence layer, unchanged by this command
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do: exiting has no effect on the task list, UI output, or storage.
    }

    /**
     * Indicates that the application should leave the main input loop after this command.
     *
     * @return true because this command terminates the program
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
