--liquibase formatted sql

--changeset faust32:1
CREATE TABLE IF NOT EXISTS user_location (
   user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
   location_id BIGINT NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
   PRIMARY KEY (user_id, location_id)
);
--rollback DROP TABLE