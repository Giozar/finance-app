package com.giozar04.users.application.exceptions;

public class UserRetrievalException extends RuntimeException {
    public UserRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
