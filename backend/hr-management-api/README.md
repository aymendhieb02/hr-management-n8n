# HR Management API

Spring Boot backend for the HR Management System.

## PostgreSQL

The application expects a local PostgreSQL database named `RH_XTENSUS`.

Local defaults are configured in `application.yml`, so the application can start locally without manually setting `DB_PASSWORD` when PostgreSQL uses the default password `admin`.

Spring Boot does not automatically load `.env.example`. That file is documentation only; copy values from it into your shell, IDE run configuration, or deployment environment when needed.

PowerShell example:

```powershell
$env:DB_PASSWORD="admin"
```

Optional PowerShell examples:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/RH_XTENSUS"
$env:DB_USERNAME="postgres"
$env:MEDICAL_CERTIFICATES_PATH="./storage/medical-certificates"
$env:JWT_SECRET="replace_with_a_secure_secret_of_at_least_32_characters"
$env:JWT_EXPIRATION_MS="3600000"
```

In IntelliJ IDEA, open the Spring Boot run configuration and add environment variables in the `Environment variables` field, for example:

```text
DB_URL=jdbc:postgresql://localhost:5432/RH_XTENSUS;DB_USERNAME=postgres;DB_PASSWORD=admin
```

For JWT authentication, add the JWT variables to the same IntelliJ `Environment variables` field:

```text
JWT_SECRET=replace_with_a_secure_secret_of_at_least_32_characters;JWT_EXPIRATION_MS=3600000
```

`JWT_EXPIRATION_MS` controls access-token lifetime in milliseconds. The default local value is `3600000` (one hour). Do not commit or reuse a production JWT secret; production must provide a secure secret through environment variables or a secrets manager.

Production must provide real environment variables for database URL, username, and password. Do not rely on local development defaults in production.

## Start The Application

From this directory:

```powershell
.\mvnw.cmd spring-boot:run
```

## Database Migrations

Flyway is enabled and runs migrations from:

```text
src/main/resources/db/migration
```

The initial schema is created by:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

Hibernate is configured with `spring.jpa.hibernate.ddl-auto=validate`, so it validates the schema after Flyway creates it.

Do not use `ddl-auto=create` or `ddl-auto=update` for this project. Schema changes should be made through Flyway migrations.
