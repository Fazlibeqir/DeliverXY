# DeliverXY Local Observability Runbook

This runbook explains how to run DeliverXY locally with Prometheus, Grafana, and k6 performance tests.

The recommended workflow uses Python helper scripts from:

```txt
scripts/perf/
```

Raw Docker and k6 commands are still included only when useful.

## 1. Prerequisites

Install:

- Python 3.10+
- Docker
- Docker Compose
- Git

Optional local tools:

- k6
- curl
- jq

You can run k6 through Docker, so local k6 installation is not required.

## 2. Start the stack

From the repository root:

```bash
python scripts/perf/start_observability.py
```

This script runs:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

Services:

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

## 3. Check observability health

Run:

```bash
python scripts/perf/check_observability.py
```

This checks:

- backend health endpoint
- backend Prometheus metrics endpoint
- Prometheus scrape target
- Grafana health endpoint

Expected output should show all checks as PASS.

You can override URLs:

```bash
python scripts/perf/check_observability.py \
  --base-url http://localhost:8080 \
  --prometheus-url http://localhost:9090 \
  --grafana-url http://localhost:3001
```

## 4. Manual backend health check

```bash
curl http://localhost:8080/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

## 5. Manual Prometheus endpoint check

```bash
curl http://localhost:8080/actuator/prometheus | head
```

You should see Prometheus-format metrics.

If this fails, check:

```bash
docker compose logs backend
```

Make sure the backend is running with the observability profile.

## 6. Verify Prometheus scrape target

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

## 7. Open Grafana dashboard

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

## 8. Run k6 smoke test

Recommended Docker-based command:

```bash
python scripts/perf/run_smoke.py --docker --summary-export
```

Local k6 command:

```bash
python scripts/perf/run_smoke.py --summary-export
```

Use an existing user instead of auto-registration:

```bash
python scripts/perf/run_smoke.py \
  --docker \
  --summary-export \
  --register-test-user false \
  --test-user-email your-user@example.com \
  --test-user-password 'your-password'
```

## 9. Run k6 load test

Recommended Docker-based command:

```bash
python scripts/perf/run_load.py --docker --summary-export
```

Local k6 command:

```bash
python scripts/perf/run_load.py --summary-export
```

Override backend URL:

```bash
python scripts/perf/run_load.py \
  --docker \
  --summary-export \
  --base-url http://localhost:8080
```

## 10. Run stress, spike, and long stability tests

Do not run these before smoke and load tests pass.

Stress:

```bash
python scripts/perf/run_stress.py --docker --summary-export
```

Spike:

```bash
python scripts/perf/run_spike.py --docker --summary-export
```

Long stability test:

```bash
python scripts/perf/run_soak.py --docker --summary-export
```

## 11. k6 result files

When `--summary-export` is used, JSON summaries are written to:

```txt
performance-tests/results/
```

Expected examples:

```txt
performance-tests/results/smoke-summary.json
performance-tests/results/load-summary.json
performance-tests/results/stress-summary.json
performance-tests/results/spike-summary.json
performance-tests/results/soak-summary.json
```

## 12. Fill the baseline report

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
- virtual users
- duration
- p95 latency
- p99 latency
- error rate
- requests per second
- backend CPU/memory observations
- database observations
- bottlenecks

## 13. Common failure cases

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
python scripts/perf/run_smoke.py \
  --docker \
  --register-test-user false \
  --test-user-email your-user@example.com \
  --test-user-password 'your-password'
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

## 14. Stop the stack

Recommended:

```bash
python scripts/perf/stop_observability.py
```

Remove volumes too:

```bash
python scripts/perf/stop_observability.py --volumes
```

The raw Docker command is:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml down
```

## 15. Recommended workflow

Use this order every time:

1. Start stack with `python scripts/perf/start_observability.py`.
2. Check observability with `python scripts/perf/check_observability.py`.
3. Open Grafana.
4. Run smoke test.
5. Run load test.
6. Export summary JSON.
7. Fill baseline report.
8. Investigate bottlenecks.
9. Apply one optimization.
10. Re-run the same test.
11. Document before/after results.
12. Stop stack.
