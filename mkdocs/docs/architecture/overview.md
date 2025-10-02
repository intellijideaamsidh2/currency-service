# System Architecture Overview

The Currency Service is built as two Spring Boot microservices that collaborate over HTTP:

- Currency Exchange Service (port 8181) manages exchange rates and persistence
- Currency Conversion Service (port 8282) calculates converted amounts using the exchange rates

```mermaid
graph TB
  subgraph Client
    UI[Client / API Consumer]
  end
  subgraph Services
    CONV[Currency Conversion\n:8282]
    EXCH[Currency Exchange\n:8181]
  end
  subgraph Data
    H2[(H2 - Dev)]
    MYSQL[(MySQL - Prod)]
  end

  UI --> CONV
  CONV -->|Feign| EXCH
  EXCH --> H2
  EXCH --> MYSQL
```

Key qualities: scalability (horizontal), resilience (Retry, CircuitBreaker), observability (Actuator, Micrometer), and portability (Docker/Kubernetes).
