package dingleberry.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that spans a start and end date/time.
 */
public class Events extends Task {
    LocalDateTime from;
    LocalDateTime to;

    /**
     * Creates an event task with a start and end time.
     *
     * @param description the task description
     * @param from the start date/time of the event
     * @param to the end date/time of the event
     */
    public Events(String description, LocalDateTime from, LocalDateTime to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the user-facing string for an event task, including its time window.
     *
     * @return the formatted event description
     */
    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)", super.toString(),
                from.format(DISPLAY_DATE_TIME_FORMAT), to.format(DISPLAY_DATE_TIME_FORMAT));
    }

    /**
     * Serializes the event into the storage format used for persisted task data.
     *
     * @return the persisted representation of this event
     */
    @Override
    public String toSaveFormat() {
        return "E | " + encodeCommonFields() + " | " + from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + " | " + to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
