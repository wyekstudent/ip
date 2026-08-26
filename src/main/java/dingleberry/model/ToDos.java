package dingleberry.model;

/**
 * Represents a simple to-do task without any date or time constraints.
 */
public class ToDos extends Task {
    /**
     * Creates a to-do task with the given description.
     *
     * @param description the task description
     */
    public ToDos(String description) {
        super(description);
    }

    /**
     * Returns the user-facing string representation for a to-do task.
     *
     * @return the formatted to-do string
     */
    @Override
    public String toString() {
        return String.format("[T]%s", super.toString());
    }

    /**
     * Serializes the to-do task for persistence.
     *
     * @return the persisted representation of this task
     */
    @Override
    public String toSaveFormat() {
        return "T | " + encodeCommonFields();
    }
}
