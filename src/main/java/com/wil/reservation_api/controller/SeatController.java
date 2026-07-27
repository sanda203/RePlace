package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.seat.CreateSeatsRequest;
import com.wil.reservation_api.dto.seat.SeatResponse;
import com.wil.reservation_api.service.SeatService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events/{eventId}/seats")
public class SeatController {
    private final SeatService seatService;


    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public List<SeatResponse> getSeatsForEvent(@PathVariable UUID eventId){
        return seatService.getSeatsForEvent(eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<SeatResponse> createSeats(@Valid @RequestBody CreateSeatsRequest request, @PathVariable UUID eventId){
        return seatService.addSeats(eventId, request.labels());
    }
}
