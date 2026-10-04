package com.giozar04.users.application.exceptions;

public class UserAuthenticationException extends RuntimeException {
    public UserAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
