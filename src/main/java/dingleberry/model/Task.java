package dingleberry.model;

import java.time.format.DateTimeFormatter;

/**
 * Represents a task with a description and completion status.
 */
public abstract class Task {
    /** Shared format used to display date/times, e.g. "Dec 2 2019, 6:00 pm". */
    public static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy, h:mm a");

    /** Stores the user-visible description. */
    private String description;
    /** Tracks whether this task is complete. */
    private boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param taskDescription the user-visible description.
     */
    public Task(final String taskDescription) {
        this.description = taskDescription;
        this.isDone = false;
    }

    /**
     * Returns the completion-status icon displayed with this task.
     *
     * @return "X" when complete; otherwise, a space.
     */
    public final String getStatusIcon() {
        return (isDone ? "X" : " ");
    }

    /** Marks this task as complete. */
    public final void markAsDone() {
        isDone = true;
    }

    /** Marks this task as incomplete. */
    public final void unmarkDone() {
        isDone = false;
    }

    /**
     * Returns the user-visible description of this task.
     *
     * @return the task description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Returns whether this task is complete.
     *
     * @return true if this task is complete.
     */
    public final boolean isDone() {
        return isDone;
    }

    /**
    * Encodes the fields common to every task type as "doneFlag | description"
    * for use by subclasses building their {@link #toSaveFormat()} line.
    *
    * @return the encoded common task fields.
     */
    protected final String encodeCommonFields() {
        return String.format("%s | %s", isDone ? "1" : "0", description);
    }

    /**
        * Encodes this task as a single line for persistence in the data file.
        *
        * @return the persisted task representation.
     */
    public abstract String toSaveFormat();

    /**
     * Returns a user-facing representation. Subclasses should prepend their
     * type marker and include this representation.
     *
     * @return the status and description for this task.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", this.getStatusIcon(), this.description);
    }
}
