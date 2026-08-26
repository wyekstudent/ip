package dingleberry.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that must be done before a specific date and time.
 */
public class Deadlines extends Task {

    private final LocalDateTime by;

    /** Creates an incomplete task due at the given date and time. */
    public Deadlines(String description, LocalDateTime by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toString() {
        return String.format("[D]%s (by: %s)", super.toString(), by.format(DISPLAY_DATE_TIME_FORMAT));
    }

    @Override
    public String toSaveFormat() {
        return "D | " + encodeCommonFields() + " | " + by.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
