# Capital Cruise Backend

Backend Spring Boot 3.5 for the Capital Cruise platform.

## Branches

- Productive branch: `main-app`

## Local Development

- Spring profiles default to `dev`.
- The app listens on `8080` locally unless `PORT` is set.
- Swagger is available at `/swagger-ui/index.html`.

## Deploy to Google Cloud Run

See [docs/deployment-cloud-run.md](docs/deployment-cloud-run.md).

Main entrypoints:

- Bootstrap GCP: `.\scripts\gcp\bootstrap-cloud-run.ps1`
- Manual initial deploy: `.\scripts\gcp\deploy-initial.ps1`
- Automatic deploy: `.github/workflows/deploy-cloud-run.yml`

## Health

- App health: `GET /api/v1/health`
- Actuator health: `GET /actuator/health`

## Notes

- No `.env` file is required in production.
- Secrets are loaded from Google Secret Manager in Cloud Run.
- JWT remains enabled through the existing Spring Security setup.
