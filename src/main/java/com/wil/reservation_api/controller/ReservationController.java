package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.ReservationRequest;
import com.wil.reservation_api.dto.ReservationResponse;
import com.wil.reservation_api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody ReservationRequest reservationRequest){
        return this.reservationService.createReservation(reservationRequest.userId(), reservationRequest.eventId(), reservationRequest.seatIds());
    }

}
