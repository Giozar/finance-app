package com.giozar04.categories.application.exceptions;

public class CategoryCreationException extends RuntimeException {
    public CategoryCreationException(String message) {
        super(message);
    }

    public CategoryCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
