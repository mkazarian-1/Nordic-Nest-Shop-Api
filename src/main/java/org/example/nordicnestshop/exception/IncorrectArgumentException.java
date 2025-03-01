package org.example.nordicnestshop.exception;

public class IncorrectArgumentException extends RuntimeException {
    public IncorrectArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectArgumentException(String message) {
        super(message);
    }
}
