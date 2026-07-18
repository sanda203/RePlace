package com.wil.reservation_api.service;

import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.exception.EventNotFoundException;
import com.wil.reservation_api.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event getByIdOrThrow(UUID id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException("Event not found: " + id));
    }
}
