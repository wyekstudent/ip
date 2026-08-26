import java.time.format.DateTimeFormatter;

/**
 * Represents a task with a description and completion status.
 */
public abstract class Task {
    /** Shared format used to display date/times to the user, e.g. "Dec 2 2019, 6:00 pm". */
    public static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy, h:mm a");

    private String description;
    private boolean isDone;

    /**
     * Creates a task with the given description and an initial incomplete status.
     *
     * @param description the user-visible description of the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the status marker used in task display strings.
     *
     * @return "X" for completed tasks and a blank space for incomplete items
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }

    /** Marks the task as done. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks the task as not done. */
    public void unmarkDone() {
        isDone = false;
    }

    /**
     * Returns the task's description text.
     *
     * @return the stored description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Reports whether the task has been completed.
     *
     * @return true if the task is marked done
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Encodes the fields common to every task type as "doneFlag | description",
     * for use by subclasses building their {@link #toSaveFormat()} line.
     *
     * @return the common persisted data for this task
     */
    protected String encodeCommonFields() {
        return String.format("%s | %s", isDone ? "1" : "0", description);
    }

    /**
     * Encodes this task as a single line for persistence in the data file.
     *
     * @return the data-file representation of this task
     */
    public abstract String toSaveFormat();

    /**
     * Returns the user-facing string representation for this task.
     *
     * @return the formatted status and description
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", this.getStatusIcon(), this.description);
    }
}
