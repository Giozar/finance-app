package com.giozar04.categories.application.exceptions;

public class CategoryRetrievalException extends RuntimeException {
    public CategoryRetrievalException(String message) {
        super(message);
    }

    public CategoryRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
