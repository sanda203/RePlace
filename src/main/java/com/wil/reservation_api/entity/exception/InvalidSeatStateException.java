package com.wil.reservation_api.entity.exception;

public class InvalidSeatStateException extends RuntimeException{
    public InvalidSeatStateException(String message) {
        super(message);
    }
}
