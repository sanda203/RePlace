package com.wil.reservation_api.entity;

import com.wil.reservation_api.entity.exception.InvalidEntityStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeatTest {

    private Seat seatWithStatus(SeatStatus status) {
        return new Seat("A1", status, null);
    }

    @Test
    void givenAvailableSeat_whenHold_thenStatusBecomesHeld() {
        Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

        seat.hold();

        assertEquals(SeatStatus.HELD, seat.getStatus());
    }

    @Test
    void givenHeldSeat_whenHold_thenThrowsInvalidEntityState() {
        Seat seat = seatWithStatus(SeatStatus.HELD);

        assertThrows(InvalidEntityStateException.class, seat::hold);
    }

    @Test
    void givenBookedSeat_whenHold_thenThrowsInvalidEntityState() {
        Seat seat = seatWithStatus(SeatStatus.BOOKED);

        assertThrows(InvalidEntityStateException.class, seat::hold);
    }

    @Test
    void givenHeldSeat_whenBook_thenStatusBecomesBooked() {
        Seat seat = seatWithStatus(SeatStatus.HELD);

        seat.book();

        assertEquals(SeatStatus.BOOKED, seat.getStatus());
    }

    @Test
    void givenAvailableSeat_whenBook_thenThrowsInvalidEntityState() {
        Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

        assertThrows(InvalidEntityStateException.class, seat::book);
    }

    @Test
    void givenBookedSeat_whenBook_thenThrowsInvalidEntityState() {
        Seat seat = seatWithStatus(SeatStatus.BOOKED);

        assertThrows(InvalidEntityStateException.class, seat::book);
    }

    @Test
    void givenHeldSeat_whenRelease_thenStatusBecomesAvailable() {
        Seat seat = seatWithStatus(SeatStatus.HELD);

        seat.release();

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
    }

    @Test
    void givenAvailableSeat_whenRelease_thenThrowsInvalidEntityState() {
        Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

        assertThrows(InvalidEntityStateException.class, seat::release);
    }

    @Test
    void givenBookedSeat_whenRelease_thenThrowsInvalidEntityState() {
        Seat seat = seatWithStatus(SeatStatus.BOOKED);

        assertThrows(InvalidEntityStateException.class, seat::release);
    }
}
