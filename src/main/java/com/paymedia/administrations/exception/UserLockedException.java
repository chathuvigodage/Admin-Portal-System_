package com.paymedia.administrations.exception;

public class UserLockedException extends RuntimeException {
    public UserLockedException(String message) {
        super(message);
    }
}
