package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.seat.SeatResponse;
import com.wil.reservation_api.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final EventService eventService;

    public SeatService(SeatRepository seatRepository, EventService eventService) {
        this.seatRepository = seatRepository;
        this.eventService = eventService;
    }

    public List<SeatResponse> getSeatsForEvent (UUID eventId){
        eventService.getByIdOrThrow(eventId);
        return seatRepository.findByEventId(eventId).stream()
                .map(seat -> new SeatResponse(seat.getId(), seat.getLabel(), seat.getStatus()))
                .toList();
    }
}
