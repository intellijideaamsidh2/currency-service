# Currency Service

Two microservices:

- Currency Exchange Service: provides exchange rates (8181)
- Currency Conversion Service: converts amounts using exchange rates (8282)

```mermaid
graph TB
  Client[Client] --> Conversion[Currency Conversion\n:8282]
  Conversion -->|Feign| Exchange[Currency Exchange\n:8181]
  Exchange --> DB[(H2/MySQL)]
```

Open the Architecture section to explore more details.
