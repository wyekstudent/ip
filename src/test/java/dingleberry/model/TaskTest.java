package dingleberry.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the observable behavior shared by all {@link Task} implementations. */
class TaskTest {
    @Test
    void constructor_descriptionProvided_preservesDescription() {
        Task task = new ToDos("submit assignment");

        assertEquals("submit assignment", task.getDescription());
    }

    @Test
    void markAsDone_taskNotDone_marksTaskDone() {
        Task task = new ToDos("submit assignment");

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void markAsDone_alreadyDone_remainsDone() {
        Task task = new ToDos("submit assignment");
        task.markAsDone();

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void newTask_beforeMarkAsDone_isNotDone() {
        Task task = new ToDos("submit assignment");

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void unmarkDone_doneTask_marksTaskNotDone() {
        Task task = new ToDos("submit assignment");
        task.markAsDone();

        task.unmarkDone();

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void unmarkDone_alreadyNotDone_remainsNotDone() {
        Task task = new ToDos("submit assignment");

        task.unmarkDone();

        assertFalse(task.isDone());
    }

    @Test
    void toString_taskNotDone_includesPendingStatusAndDescription() {
        Task task = new ToDos("submit assignment");

        assertEquals("[T][ ] submit assignment", task.toString());
    }

    @Test
    void toString_doneTask_includesCompletedStatusAndDescription() {
        Task task = new ToDos("submit assignment");
        task.markAsDone();

        assertEquals("[T][X] submit assignment", task.toString());
    }

    @Test
    void toString_emptyDescription_preservesEmptyDescription() {
        Task task = new ToDos("");

        assertEquals("[T][ ] ", task.toString());
    }

    @Test
    void toSaveFormat_taskNotDone_encodesPendingStatusAndDescription() {
        Task task = new ToDos("submit assignment");

        assertEquals("T | 0 | submit assignment", task.toSaveFormat());
    }

    @Test
    void toSaveFormat_doneTask_encodesCompletedStatusAndDescription() {
        Task task = new ToDos("submit assignment");
        task.markAsDone();

        assertEquals("T | 1 | submit assignment", task.toSaveFormat());
    }
}