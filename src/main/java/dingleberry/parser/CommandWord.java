package dingleberry.parser;

/**
 * Represents a command understood by Dingleberry.
 */
public enum CommandWord {
    BYE("bye"),
    LIST("list"),
    FIND("find"),
    DELETE("delete"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event");

    private final String keyword;

    CommandWord(String keyword) {
        this.keyword = keyword;
    }

    /** Returns the text that begins this command. */
    public String keyword() {
        return keyword;
    }

    /** Returns whether the input is exactly this command's keyword. */
    public boolean isExactInput(String input) {
        return input.equalsIgnoreCase(keyword);
    }

    /** Returns the command identified by the input, or null when none matches. */
    public static CommandWord fromInput(String input) {
        for (CommandWord command : values()) {
            if (command.isExactInput(input) || command != BYE && input.regionMatches(true, 0, command.keyword + " ", 0,
                    command.keyword.length() + 1)) {
                return command;
            }
        }
        return null;
    }
}