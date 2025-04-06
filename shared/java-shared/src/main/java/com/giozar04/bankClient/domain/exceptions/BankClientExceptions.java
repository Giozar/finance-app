package com.giozar04.bankClient.domain.exceptions;

public class BankClientExceptions {

    public static class BankClientCreationException extends RuntimeException {
        public BankClientCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class name extends RuntimeException {
        public name( String message, Throwable cause) {
            super( message, cause);
        }
    }

}
