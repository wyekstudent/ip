package dingleberry.parser;

/**
 * Represents a command understood by Dingleberry.
 */
public enum CommandWord {
    BYE("bye"),
    LIST("list"),
    DELETE("delete"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event");

    private final String keyword;

    /**
     * Creates a command word for the given keyword literal.
     *
     * @param keyword the lowercase command keyword entered by the user
     */
    CommandWord(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the canonical keyword for this command.
     *
     * @return the command text used in user input
     */
    public String keyword() {
        return keyword;
    }

    /**
     * Checks whether a raw input string exactly matches this command, ignoring case.
     *
     * @param input the text to compare against the command keyword
     * @return true if the input matches this command exactly
     */
    public boolean isExactInput(String input) {
        return input.equalsIgnoreCase(keyword);
    }

    /**
     * Finds the command represented by a user-entered token or phrase.
     *
     * @param input the full raw user input
     * @return the matching command if recognized, or null otherwise
     */
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