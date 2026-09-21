CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL
        CHECK (category IN ('MUSIC','SPORTS','TECHNOLOGY','EDUCATION','CULTURE','ENTERTAINMENT')),
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('DRAFT','PUBLISHED','SOLD_OUT','CANCELLED','FINISHED')),
    event_date TIMESTAMP NOT NULL,
    minimum_age INT,
    venue_id BIGINT NOT NULL REFERENCES venues(id)
);

CREATE TABLE event_artists (
    event_id BIGINT NOT NULL REFERENCES events(id),
    artist_id BIGINT NOT NULL REFERENCES artists(id),
    PRIMARY KEY (event_id, artist_id)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL
        CHECK (type IN ('GENERAL','VIP','BACKSTAGE','STUDENT')),
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('RESERVED','PAID','CANCELLED','USED')),
    purchase_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    event_id BIGINT NOT NULL REFERENCES events(id)
);