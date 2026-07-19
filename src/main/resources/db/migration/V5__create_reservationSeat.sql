CREATE TABLE reservation_seats (
    id UUID PRIMARY KEY ,
    reservation_id UUID NOT NULL REFERENCES reservations(id),
    seat_id UUID NOT NULL REFERENCES seats(id),
    CONSTRAINT uq_reservation_seat UNIQUE (reservation_id, seat_id)
)