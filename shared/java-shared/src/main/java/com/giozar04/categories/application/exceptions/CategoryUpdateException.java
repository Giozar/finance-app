package com.giozar04.categories.application.exceptions;

public class CategoryUpdateException extends RuntimeException {
    public CategoryUpdateException(String message) {
        super(message);
    }

    public CategoryUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
