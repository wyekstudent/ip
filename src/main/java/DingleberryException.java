/**
 * Represents an invalid command or task input entered for Dingleberry.
 */
public class DingleberryException extends Exception {
    private final ErrorType errorType;

    /**
     * Creates an exception for a parameter-validation failure.
     *
     * @param message the user-facing error description
     */
    public DingleberryException(String message) {
        this(message, ErrorType.INCORRECT_PARAMETERS);
    }

    /**
     * Creates an exception with a specific classification for UI handling.
     *
     * @param message the user-facing error description
     * @param errorType the category of the issue
     */
    public DingleberryException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * Reports whether this failure was caused by an unrecognized command.
     *
     * @return true if the command name itself was invalid
     */
    public boolean isWrongCommand() {
        return errorType == ErrorType.WRONG_COMMAND;
    }

    /**
     * Classifies whether the parse error came from a wrong command name or invalid parameters.
     */
    public enum ErrorType {
        INCORRECT_PARAMETERS,
        WRONG_COMMAND
    }
}