package dingleberry.model;

import java.util.ArrayList;

/**
 * Wraps the in-memory list of tasks and the operations Dingleberry performs
 * on it (adding, deleting, looking up, and counting tasks), so callers don't
 * manipulate a raw {@link ArrayList} directly.
 */
public class TaskList {
    /** Stores the ordered collection of tasks. */
    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Wraps an already-loaded list of tasks, e.g. one read from storage.
     *
     * @param loadedTasks the tasks to manage.
     */
    public TaskList(final ArrayList<Task> loadedTasks) {
        this.tasks = loadedTasks;
    }

    /**
     * Adds a task to the end of this list.
     *
     * @param task the task to append.
     */
    public void add(final Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns the task at the given zero-based index.
     *
     * @param index the zero-based position to remove.
     * @return the removed task.
     */
    public Task remove(final int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the task at the given zero-based index.
     *
     * @param index the zero-based position to access.
     * @return the task at the specified position.
     */
    public final Task get(final int index) {
        return tasks.get(index);
    }

    /**
     * Returns the number of tasks in this list.
     *
     * @return the task count.
     */
    public final int size() {
        return tasks.size();
    }

    /**
     * Returns tasks whose descriptions contain the keyword, ignoring case.
     *
     * @param keyword the text to search for.
     * @return a list of matching tasks.
     */
    public TaskList findByKeyword(final String keyword) {
        final ArrayList<Task> filteredTasks = new ArrayList<>();
        final String keywordLowerCase = keyword.toLowerCase();
        for (Task task : tasks) {
                if (task.getDescription().toLowerCase()
                    .contains(keywordLowerCase)) {
                filteredTasks.add(task);
            }
        }
        return new TaskList(filteredTasks);
    }

    /**
     * Exposes the underlying list, e.g. for {@link Storage} to persist.
     *
     * @return the managed task collection.
     */
    public ArrayList<Task> asArrayList() {
        return tasks;
    }
}
