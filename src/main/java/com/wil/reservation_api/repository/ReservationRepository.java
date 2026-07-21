package com.wil.reservation_api.repository;

import com.wil.reservation_api.entity.Reservation;
import com.wil.reservation_api.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant now);
}