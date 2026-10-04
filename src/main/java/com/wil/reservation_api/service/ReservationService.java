package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.reservation.ReservationResponse;
import com.wil.reservation_api.entity.*;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.entity.exception.ReservationAccessDeniedException;
import com.wil.reservation_api.entity.exception.SeatEventMismatchException;
import com.wil.reservation_api.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final SeatRepository seatRepository;
    private final EventService eventService;
    private final ReservationSeatRepository reservationSeatRepository;
    private final Duration holdDuration;

    public ReservationService(ReservationRepository reservationRepository, UserService userService, SeatRepository seatRepository, EventService eventService, ReservationSeatRepository reservationSeatRepository, @Value("${app.reservation.hold-duration}") Duration holdDuration) {
        this.reservationRepository = reservationRepository;
        this.userService = userService;
        this.seatRepository = seatRepository;
        this.eventService = eventService;
        this.reservationSeatRepository = reservationSeatRepository;
        this.holdDuration = holdDuration;
    }


    @Transactional
    public ReservationResponse createReservation(UUID userId, UUID eventId, List<UUID> seatIds) {
        User user = userService.getByIdOrThrow(userId);

        Event event = eventService.getByIdOrThrow(eventId);

        List<Seat> seats = lockAndValidateSeats(eventId, seatIds);

        Reservation reservation = new Reservation(user, ReservationStatus.PENDING, Instant.now().plus(holdDuration));
        reservationRepository.save(reservation);

        holdSeatsAndLink(reservation, seats);

        return new ReservationResponse( reservation.getId(), reservation.getStatus(), reservation.getExpiresAt(), seats.stream().map(Seat::getId).toList());
    }

    @Transactional(readOnly = true)
    public List<UUID> findExpiredReservationIds() {
        return reservationRepository.findExpiredIds(ReservationStatus.PENDING, Instant.now());
    }

    @Transactional
    public void expireReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null || reservation.getStatus() != ReservationStatus.PENDING) {
            return;
        }
        reservation.expire();
        reservationSeatRepository.findByReservationId(reservationId).stream()
                .map(ReservationSeat::getSeat)
                .forEach(Seat::release);
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

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsForUser(UUID userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        List<UUID> ids = reservations.stream().map(Reservation::getId).toList();

        Map<UUID, List<UUID>> seatIdsByReservation = new HashMap<>();
        for (ReservationSeat reservationSeat : reservationSeatRepository.findByReservationIdIn(ids)) {
            UUID reservationId = reservationSeat.getReservation().getId();
            UUID seatId = reservationSeat.getSeat().getId();
            seatIdsByReservation.computeIfAbsent(reservationId, k -> new ArrayList<>()).add(seatId);
        }

        return reservations.stream()
                .map(reservation -> new ReservationResponse(
                        reservation.getId(), reservation.getStatus(), reservation.getExpiresAt(),
                        seatIdsByReservation.getOrDefault(reservation.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID reservationId, UUID userId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        assertOwner(reservation, userId);

        List<UUID> seatIds = reservationSeatRepository.findByReservationId(reservationId).stream()
                .map(reservationSeat -> reservationSeat.getSeat().getId())
                .toList();

        return new ReservationResponse(reservation.getId(), reservation.getStatus(), reservation.getExpiresAt(), seatIds);
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
