package dingleberry.exception;

/**
 * Represents an invalid command or task input entered for Dingleberry.
 */
public class DingleberryException extends Exception {
    private final ErrorType errorType;

    /** Creates an exception for incorrect command parameters. */
    public DingleberryException(String message) {
        this(message, ErrorType.INCORRECT_PARAMETERS);
    }

    /** Creates an exception with the supplied user-input error type. */
    public DingleberryException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    /** Returns whether this exception represents an unrecognized command. */
    public boolean isWrongCommand() {
        return errorType == ErrorType.WRONG_COMMAND;
    }

    /** Categorizes user-input errors. */
    public enum ErrorType {
        INCORRECT_PARAMETERS,
        WRONG_COMMAND
    }
}