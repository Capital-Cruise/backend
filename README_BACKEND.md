# Capital Cruise Backend

Backend for the Capital Cruise vehicle credit simulation platform.

## Stack

- Java 21
- Spring Boot 3.5.x
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- JWT with `jjwt`
- PostgreSQL on Supabase
- Flyway
- Springdoc OpenAPI / Swagger UI
- H2 for tests in PostgreSQL mode
- Jakarta Validation
- Lombok

## Environment Variables

PowerShell:

```powershell
$env:SUPABASE_DB_USERNAME="postgres.cbkoepkuqcxiepeqasvr"
$env:SUPABASE_DB_PASSWORD="your-supabase-password"
$env:JWT_SECRET="DevSecretKeyForCapitalCruiseJwtMustHaveAtLeastThirtyTwoCharacters"
$env:JWT_EXPIRATION_MILLIS="3600000"
```

Do not commit `.env` or real credentials.

## Run Tests

```powershell
mvn test
```

## Run the App

```powershell
mvn spring-boot:run
```

## Swagger

- http://localhost:8080/swagger-ui/index.html

## Database

Use PostgreSQL via Supabase JDBC. Do not use Prisma, Node.js, or Supabase Auth.
