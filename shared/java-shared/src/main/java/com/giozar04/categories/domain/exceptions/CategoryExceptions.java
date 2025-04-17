package com.giozar04.categories.domain.exceptions;

public class CategoryExceptions {

    public static class CategoryCreationException extends RuntimeException {
        public CategoryCreationException(String message) {
            super(message);
        }

        public CategoryCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CategoryRetrievalException extends RuntimeException {
        public CategoryRetrievalException(String message) {
            super(message);
        }

        public CategoryRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CategoryUpdateException extends RuntimeException {
        public CategoryUpdateException(String message) {
            super(message);
        }

        public CategoryUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CategoryDeletionException extends RuntimeException {
        public CategoryDeletionException(String message) {
            super(message);
        }

        public CategoryDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CategoryNotFoundException extends RuntimeException {
        public CategoryNotFoundException(String message) {
            super(message);
        }

        public CategoryNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
