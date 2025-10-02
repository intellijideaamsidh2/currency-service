# MySQL Database Scripts (DDL + DML)

```sql
DROP TABLE IF EXISTS exchange_rate;

CREATE TABLE exchange_rate (
  id BIGINT NOT NULL AUTO_INCREMENT,
  from_currency VARCHAR(10) NOT NULL,
  to_currency VARCHAR(10) NOT NULL,
  rate DECIMAL(19,6) NOT NULL,
  last_updated TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pair (from_currency, to_currency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO exchange_rate (from_currency, to_currency, rate) VALUES
  ('USD','INR',83.50),
  ('EUR','INR',89.75),
  ('USD','EUR',0.93);
```
