package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.ReservationRequest;
import com.wil.reservation_api.dto.ReservationResponse;
import com.wil.reservation_api.security.UserPrincipal;
import com.wil.reservation_api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody ReservationRequest reservationRequest, @AuthenticationPrincipal UserPrincipal principal){
        return this.reservationService.createReservation(principal.getId(), reservationRequest.eventId(), reservationRequest.seatIds());
    }

    @PostMapping("/{id}/confirm")
    public ReservationResponse confirm(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return reservationService.confirmReservation(id, principal.getId());
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable UUID id,@AuthenticationPrincipal UserPrincipal principal) {
        return reservationService.cancelReservation(id, principal.getId());
    }

}
