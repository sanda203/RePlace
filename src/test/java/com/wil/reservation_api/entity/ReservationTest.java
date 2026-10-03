package com.wil.reservation_api.entity;

import com.wil.reservation_api.entity.exception.InvalidEntityStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationTest {

    private Reservation reservationWithStatus(ReservationStatus status) {
        return new Reservation(null, status, Instant.now());
    }

    @Test
    void givenPendingReservation_whenConfirm_thenStatusBecomesConfirmed() {
        Reservation reservation = reservationWithStatus(ReservationStatus.PENDING);

        reservation.confirm();

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void givenConfirmedReservation_whenConfirm_thenThrowsInvalidEntityState() {
        Reservation reservation = reservationWithStatus(ReservationStatus.CONFIRMED);

        assertThrows(InvalidEntityStateException.class, reservation::confirm);
    }

    @Test
    void givenExpiredReservation_whenConfirm_thenThrowsInvalidEntityState() {
        Reservation reservation = reservationWithStatus(ReservationStatus.EXPIRED);

        assertThrows(InvalidEntityStateException.class, reservation::confirm);
    }

    @Test
    void givenCancelledReservation_whenConfirm_thenThrowsInvalidEntityState() {
        Reservation reservation = reservationWithStatus(ReservationStatus.CANCELLED);

        assertThrows(InvalidEntityStateException.class, reservation::confirm);
    }

    @Test
    void givenPendingReservation_whenExpire_thenStatusBecomesExpired() {
        Reservation reservation = reservationWithStatus(ReservationStatus.PENDING);

        reservation.expire();

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
    }

    @Test
    void givenConfirmedReservation_whenExpire_thenThrowsInvalidEntityState() {
        Reservation reservation = reservationWithStatus(ReservationStatus.CONFIRMED);

        assertThrows(InvalidEntityStateException.class, reservation::expire);
    }

    @Test
    void givenExpiredReservation_whenExpire_thenThrowsInvalidEntityState() {
        Reservation reservation = reservationWithStatus(ReservationStatus.EXPIRED);

        assertThrows(InvalidEntityStateException.class, reservation::expire);
    }

    @Test
    void givenPendingReservation_whenCancel_thenStatusBecomesCancelled() {
        Reservation reservation = reservationWithStatus(ReservationStatus.PENDING);

        reservation.cancel();

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void givenConfirmedReservation_whenCancel_thenThrowsInvalidEntityState() {
        Reservation reservation = reservationWithStatus(ReservationStatus.CONFIRMED);

        assertThrows(InvalidEntityStateException.class, reservation::cancel);
    }

    @Test
    void givenCancelledReservation_whenCancel_thenThrowsInvalidEntityState() {
        Reservation reservation = reservationWithStatus(ReservationStatus.CANCELLED);

        assertThrows(InvalidEntityStateException.class, reservation::cancel);
    }
}
