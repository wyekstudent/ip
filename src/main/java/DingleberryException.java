/**
 * Represents an invalid command or task input entered for Dingleberry.
 */
public class DingleberryException extends Exception {
    private final ErrorType errorType;

    public DingleberryException(String message) {
        this(message, ErrorType.INCORRECT_PARAMETERS);
    }

    public DingleberryException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public boolean isWrongCommand() {
        return errorType == ErrorType.WRONG_COMMAND;
    }

    public enum ErrorType {
        INCORRECT_PARAMETERS,
        WRONG_COMMAND
    }
}