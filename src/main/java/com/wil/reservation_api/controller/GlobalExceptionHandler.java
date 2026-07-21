package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.ErrorResponse;
import com.wil.reservation_api.dto.ReservationResponse;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.entity.exception.InvalidEntityStateException;
import com.wil.reservation_api.entity.exception.ReservationAccessDeniedException;
import com.wil.reservation_api.entity.exception.SeatEventMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex){
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(SeatEventMismatchException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(SeatEventMismatchException ex ){
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidEntityStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(InvalidEntityStateException ex){
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ReservationAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ReservationAccessDeniedException ex){
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value(), Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
