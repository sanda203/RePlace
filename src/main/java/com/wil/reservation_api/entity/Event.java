package com.wil.reservation_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private  String name;

    @Column(name = "event_date", nullable = false)
    private Instant startsAt;

    protected Event() {}

    public Event(String name, Instant startsAt) {
        this.name = name;
        this.startsAt = startsAt;
    }
}
