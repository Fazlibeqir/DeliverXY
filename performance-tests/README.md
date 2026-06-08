# DeliverXY Performance Engineering

This folder turns DeliverXY into a practical performance-engineering lab.

It covers:

- API smoke, load, stress, spike, and soak testing with k6
- JMeter/Taurus entry points
- CI performance gates
- SLA/SLO documentation
- Baseline and bottleneck-analysis reports
- Prometheus/Grafana observability scaffolding

## Current target system

DeliverXY already has:

- Spring Boot backend
- PostgreSQL database
- Vue admin dashboard
- Docker Compose local runtime
- Kubernetes manifests and GitHub Actions CI/CD

Performance tests target the local API by default:

```bash
BASE_URL=http://localhost:8080
```

## Quick start

Start DeliverXY locally:

```bash
docker compose up -d postgres backend frontend-admin
```

Run a smoke test:

```bash
docker run --rm -i \
  --network host \
  -e BASE_URL=http://localhost:8080 \
  grafana/k6 run - < performance-tests/k6/smoke.js
```

Run a load test:

```bash
docker run --rm -i \
  --network host \
  -e BASE_URL=http://localhost:8080 \
  grafana/k6 run - < performance-tests/k6/load.js
```

On macOS/Windows Docker Desktop, replace `--network host` with:

```bash
-e BASE_URL=http://host.docker.internal:8080
```

## Environment variables

| Variable | Default | Purpose |
|---|---:|---|
| `BASE_URL` | `http://localhost:8080` | DeliverXY backend URL |
| `TEST_USER_EMAIL` | generated | Existing or test user email |
| `TEST_USER_PASSWORD` | `Password123!` | Test user password |
| `REGISTER_TEST_USER` | `true` | Register test user in k6 setup |
| `AUTH_ENABLED` | `true` | Login and use bearer token |

## Test files

| File | Purpose |
|---|---|
| `k6/smoke.js` | Minimal check that API/auth/deliveries work |
| `k6/load.js` | Normal expected traffic |
| `k6/stress.js` | Push system beyond expected traffic |
| `k6/spike.js` | Sudden traffic jump |
| `k6/soak.js` | Long-running stability test |
| `taurus/deliverxy.yml` | Taurus wrapper for k6/JMeter workflows |
| `jmeter/README.md` | JMeter plan notes |

## Performance goals

See:

- `docs/performance/slo.md`
- `docs/performance/performance-baseline-template.md`
- `docs/performance/bottleneck-analysis-template.md`

## Important notes

These tests are intentionally configurable because local data and auth setup may change. For real CI gates, first seed stable users/deliveries and then reduce thresholds gradually after measuring the baseline.
