package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.reservation.ReservationResponse;
import com.wil.reservation_api.entity.*;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.entity.exception.ReservationAccessDeniedException;
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
    public ReservationResponse createReservation(UUID userId, UUID eventId, List<UUID> seatIds) {
        User user = userService.getByIdOrThrow(userId);

        Event event = eventService.getByIdOrThrow(eventId);

        List<Seat> seats = lockAndValidateSeats(eventId, seatIds);

        Reservation reservation = new Reservation(user, ReservationStatus.PENDING, Instant.now().plus(Duration.ofMinutes(15)));
        reservationRepository.save(reservation);

        holdSeatsAndLink(reservation, seats);

        return new ReservationResponse( reservation.getId(), reservation.getStatus(), reservation.getExpiresAt(), seats.stream().map(Seat::getId).toList());
    }

    @Transactional
    public void expireOverdueReservations(){
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.PENDING, Instant.now());
        for (Reservation reservation: expiredReservations){
            reservation.expire();
            List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservationId(reservation.getId());
            List<Seat> seats = reservationSeats.stream().map(ReservationSeat::getSeat).toList();
            for (Seat seat: seats){
                seat.release();
            }
        }
    }

    @Transactional
    public ReservationResponse confirmReservation(UUID reservationId, UUID userId){
        Reservation reservation = getReservationOrThrow(reservationId);
        assertOwner(reservation, userId);

        reservation.confirm();

        List<Seat> seats = reservationSeatRepository.findByReservationId(reservationId).stream()
                .map(ReservationSeat::getSeat)
                .toList();
        for (Seat seat : seats) {
            seat.book();
        }

        return new ReservationResponse(reservation.getId(), reservation.getStatus(), reservation.getExpiresAt(),
                seats.stream().map(Seat::getId).toList());
    }

    @Transactional
    public ReservationResponse cancelReservation(UUID reservationId, UUID userId){
        Reservation reservation = getReservationOrThrow(reservationId);
        assertOwner(reservation, userId);
        reservation.cancel();

        List<Seat> seats = reservationSeatRepository.findByReservationId(reservationId).stream()
                .map(ReservationSeat::getSeat)
                .toList();
        for (Seat seat : seats) {
            seat.release();
        }

        return new ReservationResponse(reservation.getId(), reservation.getStatus(), reservation.getExpiresAt(),
                seats.stream().map(Seat::getId).toList());
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

    private Reservation getReservationOrThrow(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + reservationId));
    }

    private void assertOwner(Reservation reservation, UUID userId) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ReservationAccessDeniedException("User " + userId + " does not own this reservation");
        }
    }
}
