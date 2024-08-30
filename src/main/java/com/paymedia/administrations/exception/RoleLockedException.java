package com.paymedia.administrations.exception;

public class RoleLockedException extends RuntimeException {
    public RoleLockedException(String message) {
        super(message);
    }
}
