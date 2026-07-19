package com.wil.reservation_api.entity.exception;

public class SeatEventMismatchException extends RuntimeException {
    public SeatEventMismatchException(String message) {
        super(message);
    }
}