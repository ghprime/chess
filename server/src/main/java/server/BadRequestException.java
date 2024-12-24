package server;

public class BadRequestException extends Exception {
    BadRequestException(String message) {
        super(message);
    }

    BadRequestException() {
        super("Bad request!");
    }
}
