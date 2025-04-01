package com.giozar04.users.domain.exceptions;

public class UserExceptions {

    public static class UserCreationException extends RuntimeException {
        public UserCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UserRetrievalException extends RuntimeException {
        public UserRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UserParsingException extends RuntimeException {
        public UserParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UserUpdateException extends RuntimeException {
        public UserUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UserDeletionException extends RuntimeException {
        public UserDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class UserAuthenticationException extends RuntimeException {
        public UserAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
