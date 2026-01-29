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
| **Backend** | Spring Boot 3, Java 17, PostgreSQL, JWT |
| **Mobile** | NativeScript-Vue 3, Pinia, Mapbox |
| **Admin** | Vue 3, Vite, Tailwind, Chart.js, Leaflet |
| **Infra** | Docker, Docker Compose, Kubernetes, GitHub Actions (CI/CD) |

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

## Project structure

| Folder / file | Description |
|---------------|-------------|
| [**backend/**](backend/README.md) | Spring Boot API — auth, deliveries, wallet, KYC, earnings, admin endpoints |
| [**frontend/**](frontend/README.md) | NativeScript-Vue mobile app — client & agent flows |
| [**frontend-admin/**](frontend-admin/README.md) | Vue.js admin — dashboard, users, deliveries, map, earnings, payouts, promo codes, pricing config |
| **docker-compose.yml** | Postgres + backend + admin (pre-built images) |
| [**kubernetes/**](kubernetes/README.md) | K8s manifests for future use (ArgoCD; secrets via `kubectl create secret`) |
| **.github/workflows/** | CI (build & push images), AWS EC2 CD, optional ArgoCD sync |
| [**env.template**](env.template) | Env vars template — copy to `.env` and adjust (do not commit real secrets) |

---

## Configuration

Copy [env.template](env.template) to `.env` to set database credentials, Spring profile, and (for deployed builds) API URLs. Do not commit real secrets. Sub-project READMEs describe per-app config (e.g. `VITE_API_URL` for the admin panel, API URL for the mobile app).

---

## Deployment & CI/CD

- **CI:** On push to `main`, GitHub Actions builds backend and frontend-admin and pushes to Docker Hub.
- **AWS EC2 (current):** A workflow runs after CI, SSHs to your server, and runs `docker compose` to pull and restart (see [.github/workflows/aws-cd.yml](.github/workflows/aws-cd.yml)). Set repo secrets: `EC2_HOST`, `EC2_SSH_KEY`, `VITE_API_URL` (backend URL for the admin panel).
- **Kubernetes (future):** Manifests in [kubernetes/](kubernetes/README.md) are ready for when you have a cluster (e.g. EKS, minikube). Secrets use placeholders in the repo; create real secrets with `kubectl create secret` (see [kubernetes/README.md](kubernetes/README.md)). Optional ArgoCD sync workflow (see [.github/workflows/argocd.yml](.github/workflows/argocd.yml)); enable with repo variable `ARGOCD_ENABLED=true` and ArgoCD secrets when a cluster is available.

---

## More detail

- **API, running backend locally, DB setup** → [backend/README.md](backend/README.md)
- **Mobile app setup, build, config** → [frontend/README.md](frontend/README.md)
- **Admin panel setup, build, config** → [frontend-admin/README.md](frontend-admin/README.md)
- **Kubernetes (future), secrets, apply order** → [kubernetes/README.md](kubernetes/README.md)
