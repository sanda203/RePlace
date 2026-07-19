package com.wil.reservation_api.service;

import com.wil.reservation_api.entity.*;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.entity.exception.SeatEventMismatchException;
import com.wil.reservation_api.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final SeatRepository seatRepository;
    private final EventService eventService;
    private final ReservationSeatRepository reservationSeatRepository;

    public ReservationService(ReservationRepository reservationRepository, UserService userService, SeatRepository seatRepository, EventService eventService, ReservationSeatRepository reservationSeatRepository) {
        this.reservationRepository = reservationRepository;
        this.userService = userService;
        this.seatRepository = seatRepository;
        this.eventService = eventService;
        this.reservationSeatRepository = reservationSeatRepository;
    }


    @Transactional
    public Reservation createReservation(UUID userId, UUID eventId, List<UUID> seatIds) {
        User user = userService.getByIdOrThrow(userId);

        Event event = eventService.getByIdOrThrow(eventId);

        List<Seat> seats = lockAndValidateSeats(eventId, seatIds);

        Reservation reservation = new Reservation(user, ReservationStatus.PENDING, Instant.now().plus(Duration.ofMinutes(15)));
        reservationRepository.save(reservation);

        holdSeatsAndLink(reservation, seats);

        return reservation;
    }

    private List<Seat> lockAndValidateSeats(UUID eventId, List<UUID> seatIds) {
        List<UUID> sorted = seatIds.stream().sorted().toList();
        List<Seat> seats = seatRepository.findAllByIdForUpdate(sorted);

        if (seats.size() != seatIds.size()) {
            throw new EntityNotFoundException("One or more seats not found");
        }
        for (Seat seat : seats) {
            if (!seat.getEvent().getId().equals(eventId)) {
                throw new SeatEventMismatchException("Seat " + seat.getId() + " does not belong to event " + eventId);
            }
        }
        return seats;
    }

    private void holdSeatsAndLink(Reservation reservation, List<Seat> seats) {
        for (Seat seat : seats) {
            seat.hold();
            reservationSeatRepository.save(new ReservationSeat(reservation, seat));
        }
    }
}
