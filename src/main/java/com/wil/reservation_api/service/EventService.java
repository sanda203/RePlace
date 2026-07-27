package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.event.EventRequest;
import com.wil.reservation_api.dto.event.EventResponse;
import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event getByIdOrThrow(UUID id) {
        return eventRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> new EventResponse(event.getId(), event.getName(), event.getStartsAt()))
                .toList();
    }

    public EventResponse getEventById(UUID id) {
        Event event = getByIdOrThrow(id);
        return new EventResponse(event.getId(), event.getName(), event.getStartsAt());
    }

    public EventResponse createEvent(String name, Instant startsAt) {
        Event event = new Event(name, startsAt);
        eventRepository.save(event);
        return new EventResponse(event.getId(), event.getName(), event.getStartsAt());
    }
}
