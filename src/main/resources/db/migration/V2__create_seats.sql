CREATE TABLE seats(
    id UUID PRIMARY KEY ,
    label VARCHAR(20) NOT NULL ,
    status VARCHAR(20) NOT NULL ,
    event_id UUID NOT NULL REFERENCES events(id),
    CONSTRAINT chk_seat_status CHECK ( status IN ('AVAILABLE', 'HELD', 'BOOKED') ),
    CONSTRAINT uq_seat_event_label UNIQUE (event_id, label)
)