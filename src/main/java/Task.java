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

    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public String getStatusIcon() {
        return (isDone ? "X": " ");
    }

    public void markAsDone() {
        isDone = true;
    }

    public void unmarkDone() {
        isDone = false;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDone() {
        return isDone;
    }

    /**
     * Encodes the fields common to every task type as "doneFlag | description",
     * for use by subclasses building their {@link #toSaveFormat()} line.
     */
    protected String encodeCommonFields() {
        return String.format("%s | %s", isDone ? "1" : "0", description);
    }

    /**
     * Encodes this task as a single line for persistence in the data file.
     */
    public abstract String toSaveFormat();

    @Override
    public String toString() {
        return String.format("[%s] %s", this.getStatusIcon(), this.description);
    }
}
