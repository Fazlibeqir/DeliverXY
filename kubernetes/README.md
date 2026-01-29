# DeliverXY on Kubernetes

Manifests for deploying DeliverXY to any Kubernetes cluster (e.g. minikube, kind, EKS). **Future work:** use these when you have a cluster; current production deployment is EC2 + Docker Compose (see root [README.md](../README.md)). ArgoCD can sync this folder from the repo (see [.github/workflows/argocd.yml](../.github/workflows/argocd.yml)).

---

## Secrets (important)

**Do not commit real secrets.** The repo only has placeholders (`REPLACE_ME`).

- For **local/demo**: you can keep the placeholders so `kubectl apply -f kubernetes/` works; replace with real values before production.
- For **production**: create secrets outside the repo and apply them yourself:

```bash
# Create namespace first
kubectl apply -f namespace.yaml

# App secret (JWT) – use a long random string
kubectl create secret generic app-secret -n deliverxy \
  --from-literal=JWT_SECRET=$(openssl rand -base64 32) \
  --dry-run=client -o yaml | kubectl apply -f -

# Postgres password – use a strong password
kubectl create secret generic postgres-secret -n deliverxy \
  --from-literal=SPRING_DATASOURCE_PASSWORD=your-secure-db-password \
  --from-literal=POSTGRES_PASSWORD=your-secure-db-password \
  --dry-run=client -o yaml | kubectl apply -f -
```

Then apply the rest of the manifests (configmap, deployments, etc.). The secret YAML files in the repo are only for reference or demo; in production you create secrets with `kubectl create secret` (or a secret manager / CI) and never put real values in Git.

---

## Apply order

```bash
kubectl apply -f namespace.yaml
kubectl apply -f postgres-secret.yaml   # or create manually (see above)
kubectl apply -f app-secret.yaml        # or create manually (see above)
kubectl apply -f deliverxy-configmap.yaml
kubectl apply -f uploads-pvc.yaml
kubectl apply -f postgres-service.yaml
kubectl apply -f postgres-statefulset.yaml
kubectl apply -f backend-service.yaml
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-service.yaml
kubectl apply -f frontend-deployment.yaml
kubectl apply -f ingress.yaml
```

Or apply the whole folder (namespace first, then the rest):

```bash
kubectl apply -f namespace.yaml
kubectl apply -f kubernetes/
```

---

## Before first run

1. **Secrets**  
   Either create them manually (recommended for production) as above, or edit the YAML files to replace `REPLACE_ME` with real values and **do not commit** those changes.

2. **Storage**  
   `uploads-pvc.yaml` uses `storageClassName: standard`. If your cluster has no `standard` class, remove that line or set your cluster’s default.

3. **Ingress**  
   `ingress.yaml` uses `host: deliverxy.local` and `ingressClassName: nginx`. Point your DNS or `/etc/hosts` at the ingress and ensure an nginx ingress controller is installed.

4. **ArgoCD Application**  
   To deploy via ArgoCD, apply `argocd-application.yaml` from a cluster where ArgoCD is installed. The app syncs the `kubernetes/` path from this repo. Secrets in the repo are placeholders; for production, manage secrets outside Git (e.g. Sealed Secrets, External Secrets, or manual `kubectl create secret`).
