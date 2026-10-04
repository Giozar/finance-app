package com.giozar04.tags.application.exceptions;

public class TagUpdateException extends RuntimeException {
    public TagUpdateException(String message) {
        super(message);
    }

    public TagUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
