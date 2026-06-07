package com.giozar04.accountCashbackSettings.domain.exceptions;

public class AccountCashbackSettingExceptions {

    public static class AccountCashbackSettingCreationException extends RuntimeException {
        public AccountCashbackSettingCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountCashbackSettingRetrievalException extends RuntimeException {
        public AccountCashbackSettingRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountCashbackSettingUpdateException extends RuntimeException {
        public AccountCashbackSettingUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountCashbackSettingDeletionException extends RuntimeException {
        public AccountCashbackSettingDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountCashbackSettingNotFoundException extends RuntimeException {
        public AccountCashbackSettingNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AccountCashbackSettingParsingException extends RuntimeException {
        public AccountCashbackSettingParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
