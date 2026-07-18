package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.SeatResponse;
import com.wil.reservation_api.service.SeatService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
