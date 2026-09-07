package dingleberry.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dingleberry.model.Deadlines;
import dingleberry.model.Events;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.model.Todo;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;

/** Tests adding each supported task type. */
class AddCommandTest {
    /** Redirects storage writes to a temporary folder during tests. */
    @TempDir
    private Path tempDir;

    @Test
    void execute_todo_addsAndPersistsTask() throws Exception {
        assertAddedTaskIsPersisted(new Todo("read lecture notes"));
    }

    @Test
    void execute_deadline_addsAndPersistsTask() throws Exception {
        assertAddedTaskIsPersisted(new Deadlines("submit report",
            LocalDateTime.of(2026, 8, 25, 18, 0)));
    }

    @Test
    void execute_event_addsAndPersistsTask() throws Exception {
        assertAddedTaskIsPersisted(new Events("team meeting",
            LocalDateTime.of(2026, 8, 25, 14, 0),
            LocalDateTime.of(2026, 8, 25, 15, 0)));
    }

    private void assertAddedTaskIsPersisted(final Task task) throws Exception {
        TaskList tasks = new TaskList();
        Storage storage = new Storage(tempDir.resolve("dingleberry.txt")
            .toString());

        new AddCommand(task).execute(tasks, new Ui(), storage);

        assertEquals(1, tasks.size());
        assertEquals(task.getDescription(), tasks.get(0).getDescription());
        TaskList loadedTasks = new TaskList(storage.load());
        assertEquals(1, loadedTasks.size());
        assertInstanceOf(task.getClass(), loadedTasks.get(0));
    }
}