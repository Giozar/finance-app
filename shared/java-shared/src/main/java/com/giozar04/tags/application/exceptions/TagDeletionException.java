package com.giozar04.tags.application.exceptions;

public class TagDeletionException extends RuntimeException {
    public TagDeletionException(String message) {
        super(message);
    }

    public TagDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
