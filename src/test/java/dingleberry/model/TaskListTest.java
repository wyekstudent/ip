package dingleberry.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/** Tests task-list operations that work across multiple tasks. */
class TaskListTest {
    @Test
    void constructorSourceListChangesDoesNotChangeTaskList() {
        ArrayList<Task> sourceTasks = new ArrayList<>();
        sourceTasks.add(new Todo("read chapter one"));
        TaskList taskList = new TaskList(sourceTasks);

        sourceTasks.add(new Todo("buy milk"));

        assertEquals(1, taskList.size());
    }

    @Test
    void asArrayListReturnedListChangesDoesNotChangeTaskList() {
        TaskList taskList = new TaskList();
        taskList.add(new Todo("read chapter one"));
        ArrayList<Task> copiedTasks = taskList.asArrayList();

        copiedTasks.add(new Todo("buy milk"));

        assertEquals(1, taskList.size());
    }

    @Test
    void findByKeywordMatchingTasksReturnsOnlyMatches() {
        TaskList taskList = new TaskList(
            new Todo("read chapter one"),
            new Todo("buy milk"),
            new Todo("read chapter two"));

        TaskList filteredTasks = taskList.findByKeyword("read");

        assertEquals(2, filteredTasks.size());
        assertEquals("[T][ ] read chapter one",
            filteredTasks.get(0).toString());
        assertEquals("[T][ ] read chapter two",
            filteredTasks.get(1).toString());
    }

    @Test
    void findByKeywordNoMatchesReturnsEmptyList() {
        TaskList taskList = new TaskList();
        taskList.add(new Todo("read chapter one"));

        TaskList filteredTasks = taskList.findByKeyword("banana");

        assertEquals(0, filteredTasks.size());
    }

    @Test
    void findByKeywordIgnoreCaseMatchesRegardlessOfLetterCase() {
        TaskList taskList = new TaskList();
        taskList.add(new Todo("Read chapter one"));

        TaskList filteredTasks = taskList.findByKeyword("read");

        assertEquals(1, filteredTasks.size());
    }
}
