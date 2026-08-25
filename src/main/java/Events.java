import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task that spans a start and end date/time.
 */
public class Events extends Task {
    LocalDateTime from;
    LocalDateTime to;

    public Events(String description, LocalDateTime from, LocalDateTime to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString(){
        return String.format("[E]%s (from: %s to: %s)", super.toString(),
                from.format(DISPLAY_DATE_TIME_FORMAT), to.format(DISPLAY_DATE_TIME_FORMAT));
    }

    @Override
    public String toSaveFormat() {
        return "E | " + encodeCommonFields() + " | " + from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + " | " + to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
