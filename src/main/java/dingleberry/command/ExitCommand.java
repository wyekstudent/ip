package dingleberry.command;

import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Signals that the main loop should stop; the goodbye message is printed by Dingleberry once it does. */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do: exiting has no effect on the task list, UI output, or storage.
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
