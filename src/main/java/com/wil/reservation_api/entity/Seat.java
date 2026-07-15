package com.wil.reservation_api.entity;

import com.wil.reservation_api.entity.exception.InvalidSeatStateException;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "seats")
@Getter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    protected Seat(){}

    public Seat(String label, SeatStatus status, Event event ){
        this.label = label;
        this.status = status;
        this.event = event;
    }

    public void hold(){
        if(status != SeatStatus.AVAILABLE){
            throw new InvalidSeatStateException("Seat is not available for hold: current status is " + status);
        }
        this.status = SeatStatus.HELD;
    }

    public void book(){
        if(status != SeatStatus.HELD){
            throw new InvalidSeatStateException("Seat is not available for book: current status is " + status);
        }
        this.status = SeatStatus.BOOKED;
    }

    public void release(){
        if(status != SeatStatus.HELD){
            throw new InvalidSeatStateException("Seat is not available for release: current status is " + status);
        }
        this.status = SeatStatus.AVAILABLE;
    }

}
