package com.giozar04.tags.application.exceptions;

public class TagCreationException extends RuntimeException {
    public TagCreationException(String message) {
        super(message);
    }

    public TagCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
