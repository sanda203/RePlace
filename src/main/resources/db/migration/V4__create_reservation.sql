CREATE TABLE reservations(
    id UUID primary key ,
    user_id UUID NOT NULL REFERENCES users(id) ,
    status VARCHAR(20) NOT NULL ,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL ,
    CONSTRAINT chk_reservation_status CHECK ( status IN ('PENDING', 'CONFIRMED', 'EXPIRED', 'CANCELLED'))
)