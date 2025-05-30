-- users
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL
);

-- trainers
CREATE TABLE trainers (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          specialization VARCHAR(255)
);

-- training_sessions
CREATE TABLE training_sessions (
                                   id BIGSERIAL PRIMARY KEY,
                                   title VARCHAR(255) NOT NULL,
                                   description TEXT,
                                   start_time TIMESTAMP NOT NULL,
                                   end_time TIMESTAMP NOT NULL,
                                   trainer_id BIGINT NOT NULL
                                       REFERENCES trainers(id)
);

-- reservations
CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              reservation_time TIMESTAMP NOT NULL,
                              user_id BIGINT NOT NULL
                                  REFERENCES users(id),
                              session_id BIGINT NOT NULL
                                  REFERENCES training_sessions(id)
);
