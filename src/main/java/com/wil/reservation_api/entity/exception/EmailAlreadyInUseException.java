package com.wil.reservation_api.entity.exception;

public class EmailAlreadyInUseException extends RuntimeException{
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
