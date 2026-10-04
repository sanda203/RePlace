package com.wil.reservation_api.controller;

import com.wil.reservation_api.dto.event.EventRequest;
import com.wil.reservation_api.dto.seat.CreateSeatsRequest;
import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.Role;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.repository.EventRepository;
import com.wil.reservation_api.repository.ReservationRepository;
import com.wil.reservation_api.repository.ReservationSeatRepository;
import com.wil.reservation_api.repository.SeatRepository;
import com.wil.reservation_api.repository.UserRepository;
import com.wil.reservation_api.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EventAdminAuthorizationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationSeatRepository reservationSeatRepository;
    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;
    private UUID existingEventId;

    private void clean() {
        reservationSeatRepository.deleteAll();
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        clean();

        User user = userRepository.save(new User("user@test.com", "ENCODED"));
        userToken = jwtService.generateToken(user.getId());

        User admin = new User("admin@test.com", "ENCODED");
        ReflectionTestUtils.setField(admin, "role", Role.ADMIN);
        userRepository.save(admin);
        adminToken = jwtService.generateToken(admin.getId());

        existingEventId = eventRepository.save(new Event("Event", Instant.now().plus(Duration.ofDays(1)))).getId();
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    private <T> HttpEntity<T> jsonWithToken(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void givenUserRole_whenCreateEvent_thenForbidden() {
        EventRequest request = new EventRequest("Concert", Instant.now().plus(Duration.ofDays(1)));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/events", jsonWithToken(request, userToken), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void givenAdminRole_whenCreateEvent_thenCreated() {
        EventRequest request = new EventRequest("Concert", Instant.now().plus(Duration.ofDays(1)));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/events", jsonWithToken(request, adminToken), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void givenUserRole_whenCreateSeats_thenForbidden() {
        CreateSeatsRequest request = new CreateSeatsRequest(List.of("A1", "A2"));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/events/" + existingEventId + "/seats", jsonWithToken(request, userToken), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void givenNoToken_whenCreateEvent_thenUnauthorized() {
        EventRequest request = new EventRequest("Concert", Instant.now().plus(Duration.ofDays(1)));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/events", new HttpEntity<>(request, headers), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
