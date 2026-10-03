package com.wil.reservation_api.repository;

import com.wil.reservation_api.entity.Reservation;
import com.wil.reservation_api.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    @Query("SELECT r.id FROM Reservation r WHERE r.status = :status AND r.expiresAt < :now")
    List<UUID> findExpiredIds(@Param("status") ReservationStatus status, @Param("now") Instant now);

    List<Reservation> findByUserId(UUID userId);
}