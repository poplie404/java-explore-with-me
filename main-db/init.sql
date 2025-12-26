CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(512) NOT NULL UNIQUE,
    name VARCHAR(250) NOT NULL
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    annotation TEXT NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    description TEXT NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    initiator_id BIGINT REFERENCES users(id),
    paid BOOLEAN DEFAULT false,
    confirmed_requests INTEGER DEFAULT 0,
    created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN DEFAULT true,
    participant_limit INTEGER,
    state VARCHAR(20) DEFAULT 'PENDING',
    title VARCHAR(120) NOT NULL,
    views BIGINT DEFAULT 0
);

CREATE TABLE compilations (
    id BIGSERIAL PRIMARY KEY,
    pinned BOOLEAN DEFAULT false,
    title VARCHAR(120) NOT NULL
);

CREATE TABLE requests (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT REFERENCES events(id),
    requester_id BIGINT REFERENCES users(id),
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING'
);

CREATE TABLE compilation_events (
    compilation_id BIGINT REFERENCES compilations(id),
    event_id BIGINT REFERENCES events(id),
    PRIMARY KEY (compilation_id, event_id)
);
