# DeliverXY Local Observability Runbook

This runbook explains how to run DeliverXY locally with Prometheus, Grafana, and k6 performance tests.

## 1. Prerequisites

Install:

- Docker
- Docker Compose
- Git

Optional local tools:

- k6
- curl
- jq

You can also run k6 through Docker, so local k6 installation is not required.

## 2. Start the stack

From the repository root:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

This starts:

| Service | URL |
|---|---|
| Backend API | http://localhost:8080 |
| Admin panel | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |

Grafana default login:

```txt
username: admin
password: admin
```

## 3. Verify backend health

```bash
curl http://localhost:8080/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

## 4. Verify Prometheus endpoint

```bash
curl http://localhost:8080/actuator/prometheus | head
```

You should see Prometheus-format metrics.

If this fails, check:

```bash
docker compose logs backend
```

Make sure the backend is running with the observability profile.

## 5. Verify Prometheus scrape target

Open:

```txt
http://localhost:9090/targets
```

Expected target:

```txt
deliverxy-backend -> UP
```

If target is DOWN:

1. Check backend container is running.
2. Check `/actuator/prometheus` manually.
3. Check `monitoring/prometheus/prometheus.yml` target name.
4. Check Docker Compose service name is `backend`.

## 6. Open Grafana dashboard

Open:

```txt
http://localhost:3001
```

Go to:

```txt
Dashboards -> DeliverXY -> DeliverXY API Performance
```

Expected panels:

- HTTP Server Request Rate
- HTTP p95 Latency
- HTTP Error Rate
- JVM Memory Used
- JVM Threads
- HikariCP Active Connections

## 7. Run k6 smoke test

Using Docker on Linux:

```bash
docker run --rm -i \
  --network host \
  -e BASE_URL=http://localhost:8080 \
  grafana/k6 run - < performance-tests/k6/smoke.js
```

Using Docker Desktop on macOS or Windows:

```bash
docker run --rm -i \
  -e BASE_URL=http://host.docker.internal:8080 \
  grafana/k6 run - < performance-tests/k6/smoke.js
```

Using local k6:

```bash
BASE_URL=http://localhost:8080 k6 run performance-tests/k6/smoke.js
```

## 8. Run k6 load test

```bash
BASE_URL=http://localhost:8080 k6 run performance-tests/k6/load.js
```

Or with Docker Desktop:

```bash
docker run --rm -i \
  -e BASE_URL=http://host.docker.internal:8080 \
  grafana/k6 run - < performance-tests/k6/load.js
```

## 9. Save k6 summary output

Create results folder:

```bash
mkdir -p performance-tests/results
```

Run:

```bash
BASE_URL=http://localhost:8080 \
k6 run \
  --summary-export performance-tests/results/load-summary.json \
  performance-tests/k6/load.js
```

## 10. Fill the baseline report

Use:

```txt
docs/performance/performance-baseline-template.md
```

Create a dated copy:

```bash
cp docs/performance/performance-baseline-template.md \
   docs/performance/performance-baseline-local-YYYY-MM-DD.md
```

Record:

- test type
- environment
- VUs
- duration
- p95 latency
- p99 latency
- error rate
- requests per second
- backend CPU/memory observations
- database observations
- bottlenecks

## 11. Run stress and spike tests

Stress:

```bash
BASE_URL=http://localhost:8080 k6 run performance-tests/k6/stress.js
```

Spike:

```bash
BASE_URL=http://localhost:8080 k6 run performance-tests/k6/spike.js
```

Soak:

```bash
BASE_URL=http://localhost:8080 k6 run performance-tests/k6/soak.js
```

Do not run stress/spike/soak before the smoke and load tests pass.

## 12. Common failure cases

### Backend health fails

```bash
docker compose logs backend
```

Most likely causes:

- PostgreSQL not ready
- wrong database credentials
- backend image not updated
- Spring profile issue

### Login fails in k6

Check whether test user registration succeeded.

Try disabling automatic registration and using an existing user:

```bash
REGISTER_TEST_USER=false \
TEST_USER_EMAIL=your-user@example.com \
TEST_USER_IDENTIFIER=your-user@example.com \
TEST_USER_PASSWORD='your-password' \
k6 run performance-tests/k6/smoke.js
```

### Prometheus has no backend metrics

Check:

```bash
curl http://localhost:8080/actuator/prometheus
```

If missing, verify:

- `micrometer-registry-prometheus` is in `backend/pom.xml`
- `application-observability.properties` exists
- backend started with `observability` profile

### Grafana dashboard has no data

Check:

1. Prometheus datasource is healthy.
2. Prometheus target is UP.
3. Backend received traffic from k6 or manual API requests.
4. Dashboard query metric names match Spring Boot metrics.

## 13. Stop the stack

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml down
```

Remove volumes too:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml down -v
```

## 14. Recommended workflow

Use this order every time:

1. Start stack.
2. Check backend health.
3. Check `/actuator/prometheus`.
4. Check Prometheus targets.
5. Open Grafana.
6. Run k6 smoke.
7. Run k6 load.
8. Export summary JSON.
9. Fill baseline report.
10. Investigate bottlenecks.
11. Apply one optimization.
12. Re-run the same test.
13. Document before/after results.
