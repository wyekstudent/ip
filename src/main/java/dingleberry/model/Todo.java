package dingleberry.model;

/** Represents a task without a date or time requirement. */
public class Todo extends Task {
    /** Creates an incomplete todo with the given description. */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return String.format("[T]%s", super.toString());
    }

    @Override
    public String toSaveFormat() {
        return "T | " + encodeCommonFields();
    }
}
