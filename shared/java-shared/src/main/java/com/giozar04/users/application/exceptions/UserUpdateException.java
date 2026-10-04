package com.giozar04.users.application.exceptions;

public class UserUpdateException extends RuntimeException {
    public UserUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
