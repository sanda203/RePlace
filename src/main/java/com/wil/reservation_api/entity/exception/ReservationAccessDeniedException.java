package com.wil.reservation_api.entity.exception;

public class ReservationAccessDeniedException extends RuntimeException{
    public ReservationAccessDeniedException(String message) {
        super(message);
    }
}
