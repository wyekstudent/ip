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

    /**
     * Wraps an already-loaded list of tasks, e.g. one read from storage.
     *
     * @param tasks the underlying list of tasks to manage
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to append
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns the task at the given zero-based index.
     *
     * @param index the zero-based index of the task to remove
     * @return the removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the task at the given zero-based index.
     *
     * @param index the zero-based position to inspect
     * @return the task stored at that index
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns the current number of tasks in the list.
     *
     * @return the number of stored tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Exposes the underlying list, e.g. for {@link dingleberry.persistence.Storage} to persist.
     *
     * @return the underlying array-backed task list
     */
    public ArrayList<Task> asArrayList() {
        return tasks;
    }
}
