package com.wil.reservation_api.scheduler;

import com.wil.reservation_api.service.ReservationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationScheduler {
    private final ReservationService reservationService;

    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedRate = 100_000)
    public void expireOverdueReservations() {
        reservationService.expireOverdueReservations();
    }
}
