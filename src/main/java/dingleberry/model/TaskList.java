package dingleberry.model;

import java.util.ArrayList;

/**
 * Wraps the in-memory list of tasks and the operations Dingleberry performs
 * on it (adding, deleting, looking up, and counting tasks), so callers don't
 * manipulate a raw {@link ArrayList} directly.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /** Wraps an already-loaded list of tasks, e.g. one read from storage. */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /** Adds a task to the end of this list. */
    public void add(Task task) {
        tasks.add(task);
    }

    /** Removes and returns the task at the given zero-based index. */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    public Task get(int index) {
        return tasks.get(index);
    }

    public int size() {
        return tasks.size();
    }

    /** Returns a new list containing only tasks whose text contains the keyword. */
    public TaskList findByKeyword(String keyword) {
        ArrayList<Task> filteredTasks = new ArrayList<>();
        String keywordLowerCase = keyword.toLowerCase();
        for (Task task : tasks) {
            if (task.getDescription().toLowerCase().contains(keywordLowerCase)) {
                filteredTasks.add(task);
            }
        }
        return new TaskList(filteredTasks);
    }

    /** Exposes the underlying list, e.g. for {@link Storage} to persist. */
    public ArrayList<Task> asArrayList() {
        return tasks;
    }
}
