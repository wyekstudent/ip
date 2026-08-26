package dingleberry.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests task-list operations that work across multiple tasks. */
class TaskListTest {
    @Test
    void findByKeyword_matchingTasks_returnsOnlyMatches() {
        TaskList taskList = new TaskList();
        taskList.add(new ToDos("read chapter one"));
        taskList.add(new ToDos("buy milk"));
        taskList.add(new ToDos("read chapter two"));

        TaskList filteredTasks = taskList.findByKeyword("read");

        assertEquals(2, filteredTasks.size());
        assertEquals("[T][ ] read chapter one", filteredTasks.get(0).toString());
        assertEquals("[T][ ] read chapter two", filteredTasks.get(1).toString());
    }

    @Test
    void findByKeyword_noMatches_returnsEmptyList() {
        TaskList taskList = new TaskList();
        taskList.add(new ToDos("read chapter one"));

        TaskList filteredTasks = taskList.findByKeyword("banana");

        assertEquals(0, filteredTasks.size());
    }

    @Test
    void findByKeyword_ignoreCase_matchesRegardlessOfLetterCase() {
        TaskList taskList = new TaskList();
        taskList.add(new ToDos("Read chapter one"));

        TaskList filteredTasks = taskList.findByKeyword("read");

        assertEquals(1, filteredTasks.size());
    }
}
