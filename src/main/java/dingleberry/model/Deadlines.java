package dingleberry.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that must be done before a specific date and time.
 */
public class Deadlines extends Task {

    protected LocalDateTime by;

    /**
     * Creates a deadline task with the specified description and due date/time.
     *
     * @param description the task description
     * @param by the point in time by which the task should be complete
     */
    public Deadlines(String description, LocalDateTime by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the user-facing string for a deadline task, including its due date/time.
     *
     * @return the formatted deadline string
     */
    @Override
    public String toString() {
        return String.format("[D]%s (by: %s)", super.toString(), by.format(DISPLAY_DATE_TIME_FORMAT));
    }

    /**
     * Serializes the deadline task into the data-file format used by storage.
     *
     * @return the persisted representation of this task
     */
    @Override
    public String toSaveFormat() {
        return "D | " + encodeCommonFields() + " | " + by.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
