# DeliverXY

**On-demand delivery platform** — clients request deliveries, agents fulfill them, admins oversee everything. Mobile app (iOS & Android), web admin dashboard, and REST API.

---

## What it does

- **Clients** — Request deliveries, track in real time, pay via in-app wallet, rate agents.
- **Agents** — See available deliveries on a map, accept jobs, manage vehicles, complete KYC, earn per delivery.
- **Admins** — Dashboard (stats, charts, deliveries over time), users & KYC approval, payouts (per agent or batch), promo codes, pricing & commission config.

---

## Tech stack

| Layer | Stack |
|-------|--------|
| **Backend** | Spring Boot 3, Java 17, PostgreSQL, JWT, Actuator, Micrometer |
| **Mobile** | NativeScript-Vue 3, Pinia, Mapbox |
| **Admin** | Vue 3, Vite, Tailwind, Chart.js, Leaflet |
| **Infra** | Docker, Docker Compose, Kubernetes, GitHub Actions (CI/CD) |
| **Performance** | k6, Python helper scripts, Taurus/JMeter notes, Prometheus, Grafana |

---

## Quick start

**Prerequisites:** Docker and Docker Compose.

```bash
git clone https://github.com/Fazlibeqir/DeliverXY.git
cd DeliverXY
cp env.template .env   # optional; defaults work for local
docker-compose up -d
```

| Service | URL |
|---------|-----|
| Admin panel | http://localhost:3000 |
| API | http://localhost:8080 |
| API health | http://localhost:8080/actuator/health |

Images are pulled from Docker Hub; no build required. For **running backend or admin locally** (e.g. for development), see the READMEs in `backend/` and `frontend-admin/`.

---

## Performance engineering quick start

DeliverXY includes a performance engineering workflow for API testing, observability, and baseline reporting.

Start the backend, PostgreSQL, Prometheus, and Grafana:

```bash
python scripts/perf/start_observability.py
```

Check health and metrics:

```bash
python scripts/perf/check_observability.py
```

Run smoke and load tests with k6 through Docker:

```bash
python scripts/perf/run_smoke.py --docker --summary-export
python scripts/perf/run_load.py --docker --summary-export
```

Run heavier tests only after smoke and load pass:

```bash
python scripts/perf/run_stress.py --docker --summary-export
python scripts/perf/run_spike.py --docker --summary-export
python scripts/perf/run_soak.py --docker --summary-export
```

Stop the stack:

```bash
python scripts/perf/stop_observability.py
```

Observability URLs:

| Service | URL |
|---|---|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |
| Backend metrics | http://localhost:8080/actuator/prometheus |

Performance docs:

- [Performance test suite](performance-tests/README.md)
- [Local observability runbook](docs/performance/local-observability-runbook.md)
- [Performance SLOs](docs/performance/slo.md)
- [Baseline report template](docs/performance/performance-baseline-template.md)
- [Bottleneck analysis template](docs/performance/bottleneck-analysis-template.md)

---

## Project structure

| Folder / file | Description |
|---------------|-------------|
| [**backend/**](backend/README.md) | Spring Boot API — auth, deliveries, wallet, KYC, earnings, admin endpoints |
| [**frontend/**](frontend/README.md) | NativeScript-Vue mobile app — client & agent flows |
| [**frontend-admin/**](frontend-admin/README.md) | Vue.js admin — dashboard, users, deliveries, map, earnings, payouts, promo codes, pricing config |
| [**performance-tests/**](performance-tests/README.md) | k6 performance tests, Taurus/JMeter entry points, and result exports |
| [**docs/performance/**](docs/performance/local-observability-runbook.md) | SLOs, baseline templates, bottleneck analysis, and observability runbook |
| [**scripts/perf/**](scripts/perf/perf_common.py) | Python helpers for running observability and k6 tests |
| [**monitoring/**](monitoring/prometheus/prometheus.yml) | Prometheus and Grafana configuration |
| **docker-compose.yml** | Postgres + backend + admin (pre-built images) |
| **docker-compose.observability.yml** | Prometheus + Grafana observability override |
| [**kubernetes/**](kubernetes/README.md) | K8s manifests for future use (ArgoCD; secrets via `kubectl create secret`) |
| **.github/workflows/** | CI, AWS EC2 CD, optional ArgoCD sync, and performance test workflow |
| [**env.template**](env.template) | Env vars template — copy to `.env` and adjust (do not commit real secrets) |

---

## Configuration

Copy [env.template](env.template) to `.env` to set database credentials, Spring profile, and (for deployed builds) API URLs. Do not commit real secrets. Sub-project READMEs describe per-app config (e.g. `VITE_API_URL` for the admin panel, API URL for the mobile app).

---

## Deployment & CI/CD

- **CI:** On push to `main`, GitHub Actions builds backend and frontend-admin and pushes to Docker Hub.
- **Performance CI:** Pull requests touching backend or performance files can run the k6 performance workflow in [.github/workflows/performance-tests.yml](.github/workflows/performance-tests.yml).
- **AWS EC2 (current):** A workflow runs after CI, SSHs to your server, and runs `docker compose` to pull and restart (see [.github/workflows/aws-cd.yml](.github/workflows/aws-cd.yml)). Set repo secrets: `EC2_HOST`, `EC2_SSH_KEY`, `VITE_API_URL` (backend URL for the admin panel).
- **Kubernetes (future):** Manifests in [kubernetes/](kubernetes/README.md) are ready for when you have a cluster (e.g. EKS, minikube). Secrets use placeholders in the repo; create real secrets with `kubectl create secret` (see [kubernetes/README.md](kubernetes/README.md)). Optional ArgoCD sync workflow (see [.github/workflows/argocd.yml](.github/workflows/argocd.yml)); enable with repo variable `ARGOCD_ENABLED=true` and ArgoCD secrets when a cluster is available.

---

## More detail

- **API, running backend locally, DB setup** → [backend/README.md](backend/README.md)
- **Mobile app setup, build, config** → [frontend/README.md](frontend/README.md)
- **Admin panel setup, build, config** → [frontend-admin/README.md](frontend-admin/README.md)
- **Performance engineering, k6, observability** → [performance-tests/README.md](performance-tests/README.md)
- **Kubernetes (future), secrets, apply order** → [kubernetes/README.md](kubernetes/README.md)
