package com.wil.reservation_api.repository;

import com.wil.reservation_api.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, UUID> {
}
