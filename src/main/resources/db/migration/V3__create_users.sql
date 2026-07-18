CREATE TABLE users (
    id UUID PRIMARY KEY ,
    email VARCHAR(255) NOT NULL ,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
)