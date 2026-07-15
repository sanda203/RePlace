package com.wil.reservation_api.repository;

import com.wil.reservation_api.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    public List<Seat> findByEventId(UUID eventId);
}
