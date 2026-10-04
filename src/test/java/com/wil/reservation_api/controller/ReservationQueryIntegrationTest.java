package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.reservation.ReservationResponse;
import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.Seat;
import com.wil.reservation_api.entity.SeatStatus;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.repository.EventRepository;
import com.wil.reservation_api.repository.ReservationRepository;
import com.wil.reservation_api.repository.ReservationSeatRepository;
import com.wil.reservation_api.repository.SeatRepository;
import com.wil.reservation_api.repository.UserRepository;
import com.wil.reservation_api.security.JwtService;
import com.wil.reservation_api.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ReservationQueryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
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
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private JwtService jwtService;

    private String tokenOwner;
    private String tokenOther;
    private UUID ownerReservationId;
    private UUID ownerSeatId;

    @BeforeEach
    void setUp() {
        Event event = eventRepository.save(new Event("Event", Instant.now().plus(Duration.ofDays(1))));
        Seat ownerSeat = seatRepository.save(new Seat("A1", SeatStatus.AVAILABLE, event));
        Seat otherSeat = seatRepository.save(new Seat("A2", SeatStatus.AVAILABLE, event));
        ownerSeatId = ownerSeat.getId();

        User owner = userRepository.save(new User("owner@test.com", "ENCODED"));
        User other = userRepository.save(new User("other@test.com", "ENCODED"));
        tokenOwner = jwtService.generateToken(owner.getId());
        tokenOther = jwtService.generateToken(other.getId());

        ownerReservationId = reservationService
                .createReservation(owner.getId(), event.getId(), List.of(ownerSeat.getId()))
                .reservationId();
        reservationService.createReservation(other.getId(), event.getId(), List.of(otherSeat.getId()));
    }

    @AfterEach
    void tearDown() {
        reservationSeatRepository.deleteAll();
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        userRepository.deleteAll();
        eventRepository.deleteAll();
    }

    private HttpEntity<Void> bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    @Test
    void givenUserHasReservations_whenGetMyReservations_thenReturnsOnlyOwnReservations() {
        ResponseEntity<ReservationResponse[]> response = restTemplate.exchange(
                "/reservations", HttpMethod.GET, bearer(tokenOwner), ReservationResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);
        assertEquals(ownerReservationId, response.getBody()[0].reservationId());
    }

    @Test
    void givenOwnReservation_whenGetById_thenReturnsReservationWithItsSeats() {
        ResponseEntity<ReservationResponse> response = restTemplate.exchange(
                "/reservations/" + ownerReservationId, HttpMethod.GET, bearer(tokenOwner), ReservationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ownerReservationId, response.getBody().reservationId());
        assertEquals(List.of(ownerSeatId), response.getBody().seatIds());
    }

    @Test
    void givenReservationOwnedByAnotherUser_whenGetById_thenReturnsForbidden() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/reservations/" + ownerReservationId, HttpMethod.GET, bearer(tokenOther), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void givenNoToken_whenGetMyReservations_thenReturnsUnauthorized() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/reservations", HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
