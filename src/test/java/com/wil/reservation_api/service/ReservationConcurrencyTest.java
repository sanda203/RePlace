package com.wil.reservation_api.service;

import com.wil.reservation_api.dto.reservation.ReservationRequest;
import com.wil.reservation_api.entity.Event;
import com.wil.reservation_api.entity.Seat;
import com.wil.reservation_api.entity.SeatStatus;
import com.wil.reservation_api.entity.User;
import com.wil.reservation_api.repository.*;
import com.wil.reservation_api.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class ReservationConcurrencyTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationSeatRepository reservationSeatRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JwtService jwtService;

    private UUID eventId;
    private UUID seatId;
    private final List<String> tokens = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Event event = new Event("Concurrency Event", Instant.now().plus(Duration.ofDays(1)));
        eventRepository.save(event);
        eventId = event.getId();

        Seat seat = new Seat("A1", SeatStatus.AVAILABLE, event);
        seatRepository.save(seat);
        seatId = seat.getId();

        for (int i = 0; i < 100; i++) {
            User user = new User("concurrency" + i + "@test.com", "password-00");
            userRepository.save(user);
            tokens.add(jwtService.generateToken(user.getId()));
        }
    }

    @AfterEach
    void tearDown() {
        reservationSeatRepository.deleteAll();
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void onlyOneReservationSucceedsUnderConcurrentRequests() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);


        for (int i = 0; i < threadCount; i++) {
            String token = tokens.get(i);
            executor.submit(() -> {
                try {
                    startGate.await();

                    ReservationRequest request = new ReservationRequest(eventId, List.of(seatId));

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(token);
                    HttpEntity<ReservationRequest> entity = new HttpEntity<>(request, headers);

                    ResponseEntity<String> response = restTemplate.postForEntity("/reservations", entity, String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        doneGate.await();
        executor.shutdown();

        Assertions.assertEquals(1, successCount.get());
        Assertions.assertEquals(99, failureCount.get());
    }
}
