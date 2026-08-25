public class Events extends Task{
    String from;
    String to;

    public Events(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString(){
        return String.format("[E]%s (from: %s to: %s)", super.toString(), this.from, this.to);
    }

    @Override
    public String toSaveFormat() {
        return "E | " + encodeCommonFields() + " | " + from + " | " + to;
    }
}
