# Currency Conversion Service

- Port: 8282
- Responsibility: Convert amounts using exchange rates from Exchange service
- Integration: OpenFeign client to call Exchange

## Configuration

- Env var: `CURRENCY_EXCHANGE_SERVICE_URL` (e.g., `http://currency-exchange:8181` in Docker network)
- Feign client name: `currency-exchange`

## Endpoint

- GET `/currencyconversion/{from}/to/{to}/quantity/{qty}`

Response:

```json
{
  "fromCurrency":"USD",
  "toCurrency":"INR",
  "quantity":100,
  "rate":83.50,
  "convertedAmount":8350.00
}
```

## Run with Docker (with network)

```bash
docker network create mynet

docker run --name currency-exchange --network mynet -d -p 8181:8181 amsidhmicroservice/currency-exchange:latest

docker run --name currency-conversion --network mynet -d -p 8282:8282 ^
  -e CURRENCY_EXCHANGE_SERVICE_URL=http://currency-exchange:8181 ^
  amsidhmicroservice/currency-conversion:latest
```

## Kubernetes (snippet)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata: { name: currency-conversion }
spec:
  replicas: 1
  selector: { matchLabels: { app: currency-conversion } }
  template:
    metadata: { labels: { app: currency-conversion } }
    spec:
      containers:
        - name: app
          image: amsidhmicroservice/currency-conversion:latest
          env:
            - name: CURRENCY_EXCHANGE_SERVICE_URL
              value: http://currency-exchange:8181
          ports: [ { containerPort: 8282 } ]
---
apiVersion: v1
kind: Service
metadata: { name: currency-conversion }
spec:
  selector: { app: currency-conversion }
  ports: [ { port: 8282, targetPort: 8282 } ]
```
