--liquibase formatted sql

--changeset faust32:1
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     login VARCHAR(254) NOT NULL,
                                     password VARCHAR(128) NOT NULL
);
--rollback DROP TABLE

--changeset faust32:2
CREATE TABLE IF NOT EXISTS locations (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(60) NOT NULL,
                                         user_id INT NOT NULL,
                                         latitude DECIMAL NOT NULL,
                                         longitude DECIMAL NOT NULL
);
--rollback DROP TABLE

--changeset faust32:3
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE IF NOT EXISTS sessions (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        user_id INT NOT NULL,
                                        expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '2 hours') NOT NULL
);
--rollback DROP TABLE
