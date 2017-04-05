CREATE TABLE IF NOT EXISTS data (
    id SERIAL,
    level INT NOT NULL,
    x TEXT,
    w TEXT,
    y TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id)
);