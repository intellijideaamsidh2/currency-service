# Deployment Guide

Use the copy button at the top-right of each code block to copy commands quickly.

## Local

Exchange (run from repo root):

```bash
cd currency-exchange
mvn spring-boot:run
```

Conversion (run from repo root):

```bash
cd currency-conversion
mvn spring-boot:run
```

## Docker

Build images (Spring Boot buildpacks):

=== "Windows (cmd)"

```bat
cd currency-exchange
mvn clean spring-boot:build-image
cd ..\currency-conversion
mvn clean spring-boot:build-image
```

=== "Linux/macOS"

```bash
cd currency-exchange
mvn clean spring-boot:build-image
cd ../currency-conversion
mvn clean spring-boot:build-image
```

Create a Docker network:

```bash
docker network create mynet
```

Run Exchange service:

```bash
docker run --name currency-exchange --network mynet -d -p 8181:8181 amsidhmicroservice/currency-exchange:latest
```

Run Conversion service (pointing to Exchange):

=== "Windows (cmd)"

```bat
docker run --name currency-conversion --network mynet -d -p 8282:8282 ^
  -e CURRENCY_EXCHANGE_SERVICE_URL=http://currency-exchange:8181 ^
  amsidhmicroservice/currency-conversion:latest
```

=== "Linux/macOS"

```bash
docker run --name currency-conversion --network mynet -d -p 8282:8282 \
  -e CURRENCY_EXCHANGE_SERVICE_URL=http://currency-exchange:8181 \
  amsidhmicroservice/currency-conversion:latest
```

## Kubernetes

Apply the following manifests (namespace + deployments + services):

```yaml
apiVersion: v1
kind: Namespace
metadata: { name: currency }
---
apiVersion: apps/v1
kind: Deployment
metadata: { name: currency-exchange, namespace: currency }
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
metadata: { name: currency-exchange, namespace: currency }
spec:
  selector: { app: currency-exchange }
  ports: [ { port: 8181, targetPort: 8181 } ]
---
apiVersion: apps/v1
kind: Deployment
metadata: { name: currency-conversion, namespace: currency }
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
              value: http://currency-exchange.currency.svc.cluster.local:8181
          ports: [ { containerPort: 8282 } ]
---
apiVersion: v1
kind: Service
metadata: { name: currency-conversion, namespace: currency }
spec:
  selector: { app: currency-conversion }
  ports: [ { port: 8282, targetPort: 8282 } ]
```
