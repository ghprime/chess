package dataaccess.exception;

public class UnauthorizedException extends DataAccessException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
