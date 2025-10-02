# Development

This page gathers practical details for developers: base URLs, local run hints, API endpoints, and a downloadable Postman collection.

## Base URLs

- Currency Exchange (local): <http://localhost:8000>
- Currency Conversion (local): <http://localhost:8100>

## Quick run (Docker)

- Ensure both services are built and images are available.
- Run containers on a shared network (names match service discovery in examples):

```text
# Exchange
# maps host port 8000 -> container 8000
# service name: currency-exchange

# Conversion
# maps host port 8100 -> container 8100
# service name: currency-conversion
```

## Endpoints

- Exchange
  - GET /currencyexchange/{from}/to/{to}/rate
- Conversion
  - GET /currencyconversion/{from}/to/{to}/quantity/{qty}

## Postman collection

Download the collection and import into Postman: [download postman collection](postman/collection.json)

Variables:

- base_exchange: <http://localhost:8000>
- base_conversion: <http://localhost:8100>
