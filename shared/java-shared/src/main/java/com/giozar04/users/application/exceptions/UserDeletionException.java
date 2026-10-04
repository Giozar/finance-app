package com.giozar04.users.application.exceptions;

public class UserDeletionException extends RuntimeException {
    public UserDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
