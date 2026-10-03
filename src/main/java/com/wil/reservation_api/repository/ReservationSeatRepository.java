package com.wil.reservation_api.repository;

import com.wil.reservation_api.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, UUID> {
    List<ReservationSeat> findByReservationId(UUID reservationId);
    List<ReservationSeat> findByReservationIdIn(List<UUID> reservationIds);
}
