package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.event.EventRequest;
import com.wil.reservation_api.dto.event.EventResponse;
import com.wil.reservation_api.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> getEvents(){
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable UUID id){
        return eventService.getEventById(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventRequest eventRequest){
        return eventService.createEvent(eventRequest.name(), eventRequest.startsAt());
    }
}
