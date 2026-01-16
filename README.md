# Movie Tracker

Educational backend project built with Spring Boot.  
REST API for searching movies via TMDb, importing them into a local database, and managing a user movie library and watchlists.

## Features
- Movie search and import from TMDb
- User movie library (status, rating, liked)
- Watchlists with item ordering
- REST API with documented endpoints (Swagger)
- PostgreSQL with Flyway migrations
- Global exception handling

## Tech Stack
- Java 17
- Spring Boot
- Spring Web, Spring Data JPA (Hibernate)
- PostgreSQL
- Flyway
- Swagger / OpenAPI (springdoc-openapi)

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON (runtime): http://localhost:8080/v3/api-docs
- OpenAPI JSON (file): `docs/openapi.json`

## Running Locally

### Prerequisites
- Java 17+
- PostgreSQL
- TMDb API key

### Configuration
Local configuration is provided via `application-local.yml` (not committed to the repository).

Required properties:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/<db_name>
    username: <db_user>
    password: <db_password>

tmdb:
  apiKey: <your_tmdb_api_key>
```

## Database

Database schema is managed via Flyway migrations and applied automatically on application startup.

## Notes

- Authentication and authorization are not implemented in this educational version.

- TMDb API may be unavailable in some regions; VPN may be required for movie search and import functionality.