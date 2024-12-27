package dataaccess.exception;

public class BadRequestException extends DataAccessException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException() {
        super("Bad request!");
    }
}
