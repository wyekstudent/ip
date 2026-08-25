package dingleberry.model;

public class ToDos extends Task{
    public ToDos(String description) {
        super(description);
    }

    @Override
    public String toString(){
        return String.format("[T]%s", super.toString());
    }

    @Override
    public String toSaveFormat() {
        return "T | " + encodeCommonFields();
    }
}
