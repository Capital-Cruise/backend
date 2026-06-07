# Capital Cruise Backend

Backend REST de Capital Cruise.

## Stack

Java 21, Spring Boot, Spring Security JWT, Spring Data JPA, Flyway, PostgreSQL, SpringDoc OpenAPI.

## Endpoints

- `GET /api/v1/health`
- `GET /actuator/health`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /api/v1/clients/**`
- `GET /api/v1/vehicles/**`
- `GET /api/v1/reference/**`
- `GET /api/v1/operations/**`

## Deploy

Cloud Run con deploy por source en `main-app`.
