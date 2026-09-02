package dingleberry.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the observable behavior shared by all {@link Task} implementations. */
class TaskTest {
    @Test
    void taskDescriptionProvidedPreservesDescription() {
        Task task = new Todo("submit assignment");

        assertEquals("submit assignment", task.getDescription());
    }

    @Test
    void markAsDoneTaskNotDoneMarksTaskDone() {
        Task task = new Todo("submit assignment");

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void markAsDoneAlreadyDoneRemainsDone() {
        Task task = new Todo("submit assignment");
        task.markAsDone();

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void taskBeforeMarkAsDoneIsNotDone() {
        Task task = new Todo("submit assignment");

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void unmarkDoneDoneTaskMarksTaskNotDone() {
        Task task = new Todo("submit assignment");
        task.markAsDone();

        task.unmarkDone();

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void unmarkDoneAlreadyNotDoneRemainsNotDone() {
        Task task = new Todo("submit assignment");

        task.unmarkDone();

        assertFalse(task.isDone());
    }

    @Test
    void toStringTaskNotDoneIncludesPendingStatusAndDescription() {
        Task task = new Todo("submit assignment");

        assertEquals("[T][ ] submit assignment", task.toString());
    }

    @Test
    void toStringDoneTaskIncludesCompletedStatusAndDescription() {
        Task task = new Todo("submit assignment");
        task.markAsDone();

        assertEquals("[T][X] submit assignment", task.toString());
    }

    @Test
    void toStringEmptyDescriptionPreservesEmptyDescription() {
        Task task = new Todo("");

        assertEquals("[T][ ] ", task.toString());
    }

    @Test
    void toSaveFormatTaskNotDoneEncodesPendingStatusAndDescription() {
        Task task = new Todo("submit assignment");

        assertEquals("T | 0 | submit assignment", task.toSaveFormat());
    }

    @Test
    void toSaveFormatDoneTaskEncodesCompletedStatusAndDescription() {
        Task task = new Todo("submit assignment");
        task.markAsDone();

        assertEquals("T | 1 | submit assignment", task.toSaveFormat());
    }
}
