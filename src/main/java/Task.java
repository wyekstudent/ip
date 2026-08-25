/**
 * Represents a task with a description and completion status.
 */
public abstract class Task {
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
