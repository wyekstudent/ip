package dingleberry.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dingleberry.exception.DingleberryException;
import dingleberry.model.TaskList;
import dingleberry.model.Todo;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Tests the task-unmarking behavior of {@link UnmarkCommand}. */
class UnmarkCommandTest {
    /** Redirects storage writes to a temporary folder during tests. */
    @TempDir
    private Path tempDir;

    @Test
    void executeValidTaskNumberMarksTaskAsNotDone()
            throws DingleberryException {
        Todo todo = new Todo("read lecture notes");
        todo.markAsDone();
        TaskList tasks = new TaskList(todo);
        Ui ui = new Ui();
        Storage storage =
                new Storage(tempDir.resolve("dingleberry.txt").toString());

        new UnmarkCommand(1).execute(tasks, ui, storage);

        assertFalse(tasks.get(0).isDone());
    }

    @Test
    void executeOutOfRangeTaskNumberThrowsException() {
        TaskList tasks = new TaskList(new Todo("read lecture notes"));
        Ui ui = new Ui();
        Storage storage =
                new Storage(tempDir.resolve("dingleberry.txt").toString());

        assertThrows(DingleberryException.class,
                () -> new UnmarkCommand(2).execute(tasks, ui, storage));
    }
}
