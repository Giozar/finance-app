package com.giozar04.categories.application.exceptions;

public class CategoryDeletionException extends RuntimeException {
    public CategoryDeletionException(String message) {
        super(message);
    }

    public CategoryDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
