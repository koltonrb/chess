package dataaccess;

import io.javalin.http.BadRequestResponse;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
    public BadRequestException(String message, Throwable ex) {super(message, ex); }
}
