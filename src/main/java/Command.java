/**
 * Represents a command understood by Dingleberry.
 */
public enum Command {
    BYE("bye"),
    LIST("list"),
    DELETE("delete"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event");

    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
    }

    public String keyword() {
        return keyword;
    }

    public boolean isExactInput(String input) {
        return input.equalsIgnoreCase(keyword);
    }

    public static Command fromInput(String input) {
        for (Command command : values()) {
            if (command.isExactInput(input) || command != BYE && input.regionMatches(true, 0, command.keyword + " ", 0,
                    command.keyword.length() + 1)) {
                return command;
            }
        }
        return null;
    }
}