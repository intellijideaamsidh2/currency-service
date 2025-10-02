# Database Design

Single table for exchange rates owned by the Exchange service.

## Table: `exchange_rate`

- `id` BIGINT PK (auto)
- `from_currency` VARCHAR(10) NOT NULL
- `to_currency` VARCHAR(10) NOT NULL
- `rate` DECIMAL(19,6) NOT NULL
- `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- Unique constraint on (`from_currency`, `to_currency`)

Indexes:

- Unique index on currency pair

Notes:

- H2 for development and tests
- MySQL/InnoDB for production
