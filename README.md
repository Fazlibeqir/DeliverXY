# DeliverXY

DeliverXY is a crowdsourced delivery platform with frontend, backend, and PostgreSQL database services. This repository contains the full source code, Dockerization, Kubernetes manifests, and CI/CD pipelines.

## Project Structure
- `backend/` - Spring Boot backend service
- `frontend-admin/` - Vue.js admin frontend
- `docker-compose.yml` - Compose file for local development
- `kubernetes/` - Kubernetes manifests for deployment
- `.github/workflows/` - GitHub Actions workflows for CI/CD

## How to Run Locally

1. Build and run using Docker Compose:
   ```bash
   docker-compose up -d
   ```
   
2. Access frontend and backend locally on their respective ports.
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api

## Kubernetes Deployment
The app is deployed on Kubernetes using the manifests inside the kubernetes/ folder. 
You can apply them via:
```bash
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/
```
You can monitor the deployment status with:
```bash
kubectl get pods -n deliverxy
kubectl get services -n deliverxy
```
The Kubernetes cluster is managed with ArgoCD for continuous deployment.

## CI/CD Pipeline
- Continuous Integration (CI): On push to the main branch, Docker images are automatically built and pushed to Docker Hub.

- Continuous Deployment (CD):

    - For AWS EC2: Docker images are deployed automatically to the server using GitHub Actions and Docker Compose.

    - For Kubernetes: Deployment manifests are automatically synced and managed by ArgoCD.
      
## Using ngrok for ArgoCD Access and Syncing
To enable GitHub Actions to communicate with your ArgoCD server securely, expose ArgoCD via port-forwarding and ngrok:
   1. Forward the ArgoCD server port locally:
      ```bash
      kubectl -n argocd port-forward svc/argocd-server 8080:80
      ```
   2. Start ngrok to expose your local ArgoCD port to the internet:
       ```bash
      ngrok http 8080
      ```
   3. Use the generated ngrok URL (e.g., https://8657-92-53-30-197.ngrok-free.app) as your ARGOCD_SERVER secret in GitHub.
   4. GitHub Actions uses this public ngrok URL to log in to ArgoCD and sync the latest manifests automatically after each successful CI run.
      
