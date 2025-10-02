# Currency Exchange Service

- Port: 8181
- Responsibility: Manage and serve currency exchange rates
- Persistence: H2 (dev) / MySQL (prod)

## Endpoints

- GET `/currencyexchange/{from}/to/{to}/rate`
- POST `/currencyexchange` (create/update)
- GET `/actuator/health`

## Example

Request:

```http
GET /currencyexchange/USD/to/INR/rate
```

Response:

```json
{ "fromCurrency":"USD", "toCurrency":"INR", "rate":83.50 }
```

## Run with Docker

```bash
docker run --name currency-exchange -d -p 8181:8181 amsidhmicroservice/currency-exchange:latest
```

## Kubernetes (snippet)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata: { name: currency-exchange }
spec:
  replicas: 1
  selector: { matchLabels: { app: currency-exchange } }
  template:
    metadata: { labels: { app: currency-exchange } }
    spec:
      containers:
        - name: app
          image: amsidhmicroservice/currency-exchange:latest
          ports: [ { containerPort: 8181 } ]
---
apiVersion: v1
kind: Service
metadata: { name: currency-exchange }
spec:
  selector: { app: currency-exchange }
  ports: [ { port: 8181, targetPort: 8181 } ]
```
