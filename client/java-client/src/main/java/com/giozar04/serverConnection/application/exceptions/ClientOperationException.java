package com.giozar04.serverConnection.application.exceptions;

public class ClientOperationException extends Exception {

    private static final long serialVersionUID = 1L;

    public ClientOperationException(String message) {
        super(message);
    }

    public ClientOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientOperationException(Throwable cause) {
        super(cause);
    }
}
