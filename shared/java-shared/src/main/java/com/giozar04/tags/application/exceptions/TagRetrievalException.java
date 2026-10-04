package com.giozar04.tags.application.exceptions;

public class TagRetrievalException extends RuntimeException {
    public TagRetrievalException(String message) {
        super(message);
    }

    public TagRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
