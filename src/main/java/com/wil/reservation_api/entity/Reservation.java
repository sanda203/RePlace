package com.wil.reservation_api.entity;

import com.wil.reservation_api.entity.exception.InvalidEntityStateException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Reservation(){}

    public Reservation (User user, ReservationStatus reservationStatus, Instant expiresAt){
        this.user = user;
        this.status = reservationStatus;
        this.expiresAt = expiresAt;
    }

    public void confirm() {
        if (status != ReservationStatus.PENDING ) {
            throw new InvalidEntityStateException("Cannot confirm: current status is " + status);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void expire() {
        if (status != ReservationStatus.PENDING) {
            throw new InvalidEntityStateException("Cannot expire: current status is " + status);
        }
        this.status = ReservationStatus.EXPIRED;
    }

    public void cancel() {
        if (status != ReservationStatus.PENDING) {
            throw new InvalidEntityStateException("Cannot cancel: current status is " + status);
        }
        this.status = ReservationStatus.CANCELLED;
    }

}
