package com.giozar04.transactions.application.exceptions;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message, Throwable cause) { super(message, cause); }
}
