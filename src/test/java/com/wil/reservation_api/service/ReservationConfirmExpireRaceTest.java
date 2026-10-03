package com.wil.reservation_api.service;

import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.Reservation;
import com.wil.reservation_api.entity.ReservationSeat;
import com.wil.reservation_api.entity.ReservationStatus;
import com.wil.reservation_api.entity.Seat;
import com.wil.reservation_api.entity.SeatStatus;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.repository.EventRepository;
import com.wil.reservation_api.repository.ReservationRepository;
import com.wil.reservation_api.repository.ReservationSeatRepository;
import com.wil.reservation_api.repository.SeatRepository;
import com.wil.reservation_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "app.reservation.expiration-scan-rate=3600000")
class ReservationConfirmExpireRaceTest {

    private static final int ITERATIONS = 40;

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationSeatRepository reservationSeatRepository;

    @AfterEach
    void tearDown() {
        reservationSeatRepository.deleteAll();
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        userRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    void givenPendingReservationHitByConfirmAndExpireAtOnce_thenFinalStateStaysConsistent() throws InterruptedException {
        for (int i = 0; i < ITERATIONS; i++) {
            Event event = eventRepository.save(new Event("Race Event", Instant.now().plus(Duration.ofDays(1))));
            Seat seat = seatRepository.save(new Seat("A1", SeatStatus.HELD, event));
            User user = userRepository.save(new User("race" + i + "@test.com", "ENCODED"));
            Reservation reservation = reservationRepository.save(
                    new Reservation(user, ReservationStatus.PENDING, Instant.now().minus(Duration.ofMinutes(1))));
            reservationSeatRepository.save(new ReservationSeat(reservation, seat));

            UUID reservationId = reservation.getId();
            UUID userId = user.getId();
            UUID seatId = seat.getId();

            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch doneGate = new CountDownLatch(2);
            ExecutorService executor = Executors.newFixedThreadPool(2);

            executor.submit(() -> {
                try {
                    startGate.await();
                    reservationService.expireReservation(reservationId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {
                } finally {
                    doneGate.countDown();
                }
            });
            executor.submit(() -> {
                try {
                    startGate.await();
                    reservationService.confirmReservation(reservationId, userId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {
                } finally {
                    doneGate.countDown();
                }
            });

            startGate.countDown();
            doneGate.await();
            executor.shutdown();

            ReservationStatus finalStatus = reservationRepository.findById(reservationId).orElseThrow().getStatus();
            SeatStatus finalSeatStatus = seatRepository.findById(seatId).orElseThrow().getStatus();

            assertTrue(
                    finalStatus == ReservationStatus.CONFIRMED || finalStatus == ReservationStatus.EXPIRED,
                    "reservation must reach a terminal state, was " + finalStatus);

            if (finalStatus == ReservationStatus.CONFIRMED) {
                assertEquals(SeatStatus.BOOKED, finalSeatStatus);
            } else {
                assertEquals(SeatStatus.AVAILABLE, finalSeatStatus);
            }

            tearDown();
        }
    }
}
