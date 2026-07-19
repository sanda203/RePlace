package com.wil.reservation_api.entity.exception;

public class InvalidEntityStateException extends RuntimeException {
    public InvalidEntityStateException(String message) {
        super(message);
    }
}
