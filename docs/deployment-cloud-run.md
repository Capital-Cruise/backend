# Cloud Run Deployment

## Architecture

The backend is packaged as a Docker image, pushed to Artifact Registry in `us-east4`, and deployed to Cloud Run with a dedicated runtime service account.

Flow:

1. GitHub push to `main-app`
2. GitHub Actions runs tests
3. Docker image is built and pushed to Artifact Registry
4. Cloud Run deploys the new revision
5. Runtime secrets are injected from Secret Manager

## Why Artifact Registry

- Stores immutable container images close to Cloud Run.
- Integrates cleanly with Google Cloud IAM.
- Supports image versioning and rollback.

## Why Secret Manager

- Keeps database credentials, JWT secrets, and runtime config out of Git.
- Lets Cloud Run inject values at runtime without .env files.
- Supports secret rotation without changing application code.

## Why Workload Identity Federation

- No service account JSON keys are stored in GitHub.
- GitHub Actions exchanges its OIDC identity for short-lived Google credentials.
- Reduces long-lived credential risk.

## Region

`us-east4` is used for both Artifact Registry and Cloud Run to keep deployment latency and data locality consistent.

## Bootstrap

Run:

```powershell
.\scripts\gcp\bootstrap-cloud-run.ps1
```

This script:

- enables required Google Cloud APIs
- creates Artifact Registry if needed
- creates runtime and deploy service accounts
- configures IAM bindings
- creates Secret Manager secrets
- configures Workload Identity Federation for GitHub Actions

## Manual Deploy

Run:

```powershell
.\scripts\gcp\deploy-initial.ps1
```

This builds and deploys the first revision manually.

## Verification

- Cloud Run URL from the console or `gcloud run services describe capital-cruise-backend --region us-east4 --project capital-cruise`
- Health check: `/api/v1/health`
- Swagger: `/swagger-ui/index.html`
- Login: `/api/v1/auth/login`

## Logs

```powershell
gcloud run services logs read capital-cruise-backend --region us-east4 --project capital-cruise --limit 100
```

## Rollback

Use the Cloud Run console:

1. Open the service
2. Select a previous revision
3. Split traffic or migrate traffic back to that revision

## Secrets and Environment Variables

Important: the production Cloud Run revision needs a real Supabase PostgreSQL password in Secret Manager. The local placeholder used for bootstrap/testing is only for local use and will not start the production container.

Secret Manager entries:

- `capital-cruise-database-url` -> `DATABASE_URL`
- `capital-cruise-supabase-db-username` -> `SUPABASE_DB_USERNAME`
- `capital-cruise-supabase-db-password` -> `SUPABASE_DB_PASSWORD`
- `capital-cruise-jwt-secret` -> `JWT_SECRET`
- `capital-cruise-jwt-expiration-millis` -> `JWT_EXPIRATION_MILLIS`
- `capital-cruise-admin-password` -> `CAPITAL_CRUISE_ADMIN_PASSWORD`
- `capital-cruise-frontend-allowed-origins` -> `FRONTEND_ALLOWED_ORIGINS`

Other runtime env vars:

- `SPRING_PROFILES_ACTIVE=prod`
- `PORT=8080` injected by Cloud Run

## If GitHub Actions Fails WIF

Check:

1. The provider resource matches the workflow configuration.
2. The attribute condition only allows `Capital-Cruise/backend` on `refs/heads/main-app`.
3. The deploy service account has `roles/iam.workloadIdentityUser` for the GitHub principal set.
4. The GitHub repository still has `id-token: write` permission in workflow.
