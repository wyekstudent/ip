package dingleberry.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dingleberry.exception.DingleberryException;
import dingleberry.model.TaskList;
import dingleberry.model.Todo;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Tests the task-marking behavior of {@link MarkCommand}. */
class MarkCommandTest {
    /** Redirects storage writes to a temporary folder during tests. */
    @TempDir
    private Path tempDir;

    @Test
    void executeValidTaskNumberMarksTaskAsDone() throws DingleberryException {
        TaskList tasks = new TaskList(new Todo("read lecture notes"));
        Ui ui = new Ui();
        Storage storage =
                new Storage(tempDir.resolve("dingleberry.txt").toString());

        new MarkCommand(1).execute(tasks, ui, storage);

        assertTrue(tasks.get(0).isDone());
    }

    @Test
    void executeOutOfRangeTaskNumberThrowsException() {
        TaskList tasks = new TaskList(new Todo("read lecture notes"));
        Ui ui = new Ui();
        Storage storage =
                new Storage(tempDir.resolve("dingleberry.txt").toString());

        assertThrows(DingleberryException.class,
                () -> new MarkCommand(2).execute(tasks, ui, storage));
    }
}
