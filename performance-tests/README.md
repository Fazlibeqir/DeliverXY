# DeliverXY Performance Engineering

This folder turns DeliverXY into a practical performance-engineering lab.

It covers:

- API smoke, load, stress, spike, and long stability testing with k6
- Python helper scripts for local execution
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

## Recommended quick start

From the repository root, start the full local stack with backend, PostgreSQL, Prometheus, and Grafana:

```bash
python scripts/perf/start_observability.py
```

Check that everything is healthy:

```bash
python scripts/perf/check_observability.py
```

Run the smoke test:

```bash
python scripts/perf/run_smoke.py --docker --summary-export
```

Run the load test:

```bash
python scripts/perf/run_load.py --docker --summary-export
```

Stop the stack:

```bash
python scripts/perf/stop_observability.py
```

Remove volumes too:

```bash
python scripts/perf/stop_observability.py --volumes
```

## Python scripts

| Script | Purpose |
|---|---|
| `scripts/perf/start_observability.py` | Start Docker Compose stack with Prometheus and Grafana |
| `scripts/perf/check_observability.py` | Check backend health, Prometheus metrics, Prometheus targets, and Grafana |
| `scripts/perf/run_smoke.py` | Run k6 smoke test |
| `scripts/perf/run_load.py` | Run k6 load test |
| `scripts/perf/run_stress.py` | Run k6 stress test |
| `scripts/perf/run_spike.py` | Run k6 spike test |
| `scripts/perf/run_soak.py` | Run k6 long stability test |
| `scripts/perf/stop_observability.py` | Stop Docker Compose stack |
| `scripts/perf/perf_common.py` | Shared Python helper functions |

## Running test types

Smoke:

```bash
python scripts/perf/run_smoke.py --docker --summary-export
```

Load:

```bash
python scripts/perf/run_load.py --docker --summary-export
```

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

Do not run stress, spike, or long stability tests until smoke and load tests pass.

## Using local k6 instead of Docker

Install k6 locally, then omit `--docker`:

```bash
python scripts/perf/run_load.py --summary-export
```

## Environment variables and CLI options

The Python scripts support these options:

| Option | Default | Purpose |
|---|---:|---|
| `--base-url` | `http://localhost:8080` | DeliverXY backend URL |
| `--docker` | disabled | Run k6 through Docker |
| `--summary-export` | disabled | Save JSON result summary |
| `--test-user-email` | `perf-local@deliverxy.test` | Existing or test user email |
| `--test-user-password` | `Password123!` | Test user password |
| `--register-test-user` | `true` | Register test user in k6 setup |
| `--auth-enabled` | `true` | Login and use bearer token |

Example using an existing user:

```bash
python scripts/perf/run_smoke.py \
  --docker \
  --summary-export \
  --register-test-user false \
  --test-user-email your-user@example.com \
  --test-user-password 'your-password'
```

## Result files

When `--summary-export` is enabled, results are written to:

```txt
performance-tests/results/
```

Example files:

```txt
performance-tests/results/smoke-summary.json
performance-tests/results/load-summary.json
performance-tests/results/stress-summary.json
performance-tests/results/spike-summary.json
performance-tests/results/soak-summary.json
```

## Direct k6 scripts

| File | Purpose |
|---|---|
| `k6/common.js` | Shared k6 auth, payload, and request helpers |
| `k6/smoke.js` | Minimal check that API/auth/deliveries work |
| `k6/load.js` | Normal expected traffic |
| `k6/stress.js` | Push system beyond expected traffic |
| `k6/spike.js` | Sudden traffic jump |
| `k6/soak.js` | Long-running stability test |
| `taurus/deliverxy.yml` | Taurus wrapper for k6/JMeter workflows |
| `jmeter/README.md` | JMeter plan notes |

## Observability URLs

After running `start_observability.py`:

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

## Performance goals

See:

- `docs/performance/slo.md`
- `docs/performance/performance-baseline-template.md`
- `docs/performance/bottleneck-analysis-template.md`
- `docs/performance/local-observability-runbook.md`

## Important notes

These tests are intentionally configurable because local data and auth setup may change. For real CI gates, first seed stable users/deliveries and then reduce thresholds gradually after measuring the baseline.
