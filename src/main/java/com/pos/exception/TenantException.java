package com.pos.exception;

public class TenantException extends RuntimeException {
    public TenantException(String message) {
        super(message);
    }
}
