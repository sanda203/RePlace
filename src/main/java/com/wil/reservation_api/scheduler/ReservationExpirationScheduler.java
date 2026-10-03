package com.wil.reservation_api.scheduler;

import com.wil.reservation_api.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReservationExpirationScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationScheduler.class);

    private final ReservationService reservationService;

    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelayString = "${app.reservation.expiration-scan-rate:100000}")
    public void expireOverdueReservations() {
        for (UUID id : reservationService.findExpiredReservationIds()) {
            try {
                reservationService.expireReservation(id);
            } catch (Exception ex) {
                log.warn("Failed to expire reservation {}: {}", id, ex.getMessage());
            }
        }
    }
}
