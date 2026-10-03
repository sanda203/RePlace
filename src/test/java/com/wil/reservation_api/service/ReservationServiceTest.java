package com.wil.reservation_api.service;

import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.Reservation;
import com.wil.reservation_api.entity.ReservationStatus;
import com.wil.reservation_api.entity.Seat;
import com.wil.reservation_api.entity.SeatStatus;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.entity.exception.EntityNotFoundException;
import com.wil.reservation_api.entity.exception.ReservationAccessDeniedException;
import com.wil.reservation_api.entity.exception.SeatEventMismatchException;
import com.wil.reservation_api.repository.ReservationRepository;
import com.wil.reservation_api.repository.ReservationSeatRepository;
import com.wil.reservation_api.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserService userService;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private EventService eventService;
    @Mock
    private ReservationSeatRepository reservationSeatRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Event eventWithId(UUID id) {
        Event event = new Event("Event", Instant.now());
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    private Reservation reservationOwnedBy(UUID ownerId) {
        User owner = new User("owner@test.com", "ENCODED");
        ReflectionTestUtils.setField(owner, "id", ownerId);
        return new Reservation(owner, ReservationStatus.PENDING, Instant.now());
    }

    @Test
    void givenMissingSeat_whenCreateReservation_thenThrowsEntityNotFound() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        List<UUID> seatIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(userService.getByIdOrThrow(userId)).thenReturn(new User("u@test.com", "ENCODED"));
        when(eventService.getByIdOrThrow(eventId)).thenReturn(eventWithId(eventId));
        when(seatRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(List.of(new Seat("A1", SeatStatus.AVAILABLE, eventWithId(eventId))));

        assertThrows(EntityNotFoundException.class,
                () -> reservationService.createReservation(userId, eventId, seatIds));
    }

    @Test
    void givenSeatFromAnotherEvent_whenCreateReservation_thenThrowsSeatEventMismatch() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID otherEventId = UUID.randomUUID();
        List<UUID> seatIds = List.of(UUID.randomUUID());
        when(userService.getByIdOrThrow(userId)).thenReturn(new User("u@test.com", "ENCODED"));
        when(eventService.getByIdOrThrow(eventId)).thenReturn(eventWithId(eventId));
        when(seatRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(List.of(new Seat("A1", SeatStatus.AVAILABLE, eventWithId(otherEventId))));

        assertThrows(SeatEventMismatchException.class,
                () -> reservationService.createReservation(userId, eventId, seatIds));
    }

    @Test
    void givenMissingReservation_whenConfirmReservation_thenThrowsEntityNotFound() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> reservationService.confirmReservation(reservationId, UUID.randomUUID()));
    }

    @Test
    void givenReservationOwnedByAnotherUser_whenConfirmReservation_thenThrowsAccessDenied() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationOwnedBy(UUID.randomUUID())));

        assertThrows(ReservationAccessDeniedException.class,
                () -> reservationService.confirmReservation(reservationId, UUID.randomUUID()));
    }

    @Test
    void givenReservationOwnedByAnotherUser_whenCancelReservation_thenThrowsAccessDenied() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationOwnedBy(UUID.randomUUID())));

        assertThrows(ReservationAccessDeniedException.class,
                () -> reservationService.cancelReservation(reservationId, UUID.randomUUID()));
    }

    @Test
    void givenNonPendingReservation_whenExpireReservation_thenSeatsAreNotTouched() {
        UUID reservationId = UUID.randomUUID();
        Reservation confirmed = reservationOwnedBy(UUID.randomUUID());
        confirmed.confirm();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(confirmed));

        reservationService.expireReservation(reservationId);

        verifyNoInteractions(reservationSeatRepository);
    }

    @Test
    void givenMissingReservation_whenExpireReservation_thenSeatsAreNotTouched() {
        UUID reservationId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        reservationService.expireReservation(reservationId);

        verifyNoInteractions(reservationSeatRepository);
    }
}
