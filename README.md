# VertexPlacements — Campus Placement Tracker API

A Spring Boot REST API powering the VertexPlacements campus placement tracker
(companies dashboard + student applications launchpad). Built to pair directly
with the provided `index.html` frontend.

## Tech Stack

| Layer          | Technology                                             |
|----------------|---------------------------------------------------------|
| Language       | Java 17                                                  |
| Framework      | Spring Boot 3.3 (Web, Data JPA, Validation, Security)    |
| Auth           | Spring Security + JWT (jjwt)                             |
| Database       | MySQL 8                                                  |
| ORM            | Hibernate / Spring Data JPA                              |
| Docs           | Springdoc OpenAPI (Swagger UI)                           |
| Boilerplate    | Lombok                                                   |
| Build          | Maven                                                    |
| Containerized  | Docker / Docker Compose                                  |

## Architecture

Controller → Service (interface + impl) → Repository → MySQL, with DTOs at the
controller boundary (entities are never exposed directly) and a global
`@RestControllerAdvice` for consistent error responses.

```
src/main/java/com/vertexplacements/trackerapi/
├── config/          # OpenAPI, CORS, and startup data seeding
├── controller/       # @RestController endpoints
├── dto/              # Request/response DTOs (validated with @Valid)
├── entity/           # Company, Application, ApplicationStatus
├── exception/        # ResourceNotFoundException + GlobalExceptionHandler
├── repository/       # Spring Data JPA + custom JPQL
└── service/          # Interfaces + impl/ business logic
```

## Data Model

**User** — `id, fullName, email (unique), password (BCrypt hash), createdAt`
**Company** — `id, name, ctc (Double), eligibilityCriteria, visitDate`, `Many-to-One → User (owner)` — **private per account**.
**Application** — `id, studentName, studentRoll, status (enum), applyDate`, `Many-to-One → Company`, `Many-to-One → User (owner)` — **private per account**: every account only ever sees and manages the companies and applications it created.

Companies and applications are private per account — every query is scoped to
the logged-in user, and a company or application belonging to someone else
returns a 404, not a 403, so its existence is never revealed to other users.

`ApplicationStatus`: `APPLIED → SHORTLISTED → SELECTED / REJECTED` (matches the
status-dropdown pipeline in the frontend).

Every `/api/**` endpoint except `/api/auth/**` requires a valid JWT
(`Authorization: Bearer <token>`), obtained from `/api/auth/register` or
`/api/auth/login`.

---

## Getting Started (Local)

### 1. Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8 running locally (or use `docker-compose up mysql`)

### 2. Configure the database
Edit `src/main/resources/application.properties` if your MySQL credentials
differ from the defaults (`root` / `root`). The schema and database
(`vertexplacements`) are created automatically on first run.

### 3. Run it
```bash
mvn spring-boot:run
```
The API starts on **http://localhost:8080**. On a fresh database, `DataSeeder`
automatically creates:
- A **demo login**, with credentials printed to your own server console on
  first startup only (never committed anywhere) — or just register your own
  account from the login screen.
- Sample companies/applications (Google, Microsoft, Deloitte, Infosys) owned
  by that demo account, so it's demo-ready immediately.

Seeding only runs when the relevant table is empty, so it won't duplicate data
on restart.

> **Upgrading an existing deployment to per-account private data?** Companies
> and applications now require an owner column. If your database already has
> rows from before this change, clear both tables first (`DELETE FROM
> applications;` then `DELETE FROM companies;`) so the schema update can add
> the new `NOT NULL` owner column cleanly, and `DataSeeder` will repopulate
> the demo data correctly owned.

### 4. Open Swagger UI
**http://localhost:8080/swagger-ui.html** — interactive docs for every
endpoint. Log in via `POST /api/auth/login`, copy the `token` from the
response, click **Authorize** at the top of the page, and paste it in as
`Bearer <token>` to try protected endpoints directly from the browser.

### 5. Connect the frontend
The API has permissive CORS enabled for local development, so the frontend
can call `http://localhost:8080/api/...` from anywhere — including opening
`index.html` directly, or serving both from the same origin by placing it at
`src/main/resources/static/index.html`.

---

## API Reference

### Authentication

| Method | Endpoint              | Description                                    | Auth required |
|--------|------------------------|--------------------------------------------------|:---:|
| POST   | `/api/auth/register`  | Create an account, returns a JWT immediately     | No |
| POST   | `/api/auth/login`     | Log in with email + password, returns a JWT      | No |
| GET    | `/api/users/me`       | Get the logged-in user's profile                 | Yes |
| PUT    | `/api/users/me`       | Update the logged-in user's display name         | Yes |
| PUT    | `/api/users/me/password` | Change the logged-in user's password          | Yes |

**Register/Login response:**
```json
{ "token": "eyJhbGciOi...", "id": 1, "fullName": "Demo Admin", "email": "admin@vertexplacements.com" }
```

Send the token on every subsequent request:
```
Authorization: Bearer eyJhbGciOi...
```

**Change password body:**
```json
{ "currentPassword": "oldPassword123", "newPassword": "newPassword456" }
```
Returns `400 Bad Request` with `"Current password is incorrect"` if `currentPassword` doesn't match.

### Companies
*(all endpoints below require `Authorization: Bearer <token>`)*

| Method | Endpoint                              | Description                                  |
|--------|----------------------------------------|-----------------------------------------------|
| GET    | `/api/companies`                       | List all companies                            |
| GET    | `/api/companies?name=&minCtc=`         | Filter by name (contains) and/or minimum CTC  |
| GET    | `/api/companies/stats`                 | Dashboard metrics: total, highest CTC, active |
| GET    | `/api/companies/{id}`                  | Get a single company                          |
| POST   | `/api/companies`                       | Create a company                              |
| PUT    | `/api/companies/{id}`                  | Update a company                              |
| DELETE | `/api/companies/{id}`                  | Delete a company (cascades its applications)  |

**POST/PUT body:**
```json
{
  "name": "Google",
  "ctc": 42,
  "eligibilityCriteria": "CGPA > 8.0, No backlogs",
  "visitDate": "2026-03-14"
}
```

### Applications
*(all endpoints below require `Authorization: Bearer <token>`)*

| Method | Endpoint                    | Description                              |
|--------|------------------------------|--------------------------------------------|
| GET    | `/api/applications`          | List **your own** applications (with company info) |
| GET    | `/api/applications/{id}`     | Get one of your own applications           |
| POST   | `/api/applications`          | Submit a new application (status = APPLIED)|
| PATCH  | `/api/applications/{id}`     | Update status of your own application      |
| DELETE | `/api/applications/{id}`     | Delete one of your own applications        |

**POST body:**
```json
{ "studentName": "Riya Sharma", "studentRoll": "21CS1042", "companyId": 1 }
```

**PATCH body:**
```json
{ "status": "SHORTLISTED" }
```
Valid values: `APPLIED`, `SHORTLISTED`, `SELECTED`, `REJECTED`.

### Error format
All errors (validation, not-found, bad enum values) return a consistent shape:
```json
{
  "timestamp": "2026-07-05T10:15:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/companies",
  "details": ["Company name is required"]
}
```

---

## Testing

A ready-to-import Postman collection is included at
`postman/VertexPlacements.postman_collection.json`, covering every endpoint
plus deliberate validation-error and bad-enum cases. Import it into Postman
and set the `baseUrl` variable (defaults to `http://localhost:8080`).

A minimal Spring context smoke test also lives at
`src/test/java/.../PlacementTrackerApplicationTests.java`; run it with:
```bash
mvn test
```

---

## Deployment

### Option A — Docker Compose (local container, includes MySQL)
```bash
docker-compose up --build
```
This starts MySQL and the API together; the API waits for MySQL's healthcheck
before starting. App available at `http://localhost:8080`.

### Option B — Docker image only (bring your own MySQL)
```bash
docker build -t vertexplacements-api .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://<host>:3306/vertexplacements?useSSL=false&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME=<user> \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e JWT_SECRET=<a-long-random-base64-string> \
  vertexplacements-api
```

### Option C — Render.com
A `render.yaml` blueprint is included.
1. Push this project to a GitHub repository.
2. On Render: **New → Blueprint**, connect the repo, and Render will read
   `render.yaml` automatically.
3. Provision a MySQL database (Render, PlanetScale, or any managed MySQL) and
   fill in `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
   `SPRING_DATASOURCE_PASSWORD` as environment variables in the Render
   dashboard (they're marked `sync: false` so you set them manually, keeping
   credentials out of git).
4. Deploy. Render builds the Dockerfile and runs the container; healthcheck
   hits `/api/companies`.

The `application-prod.properties` profile (`SPRING_PROFILES_ACTIVE=prod`)
reads every credential from environment variables — no secrets are ever
committed to source control.

---

## Roadmap Recap

- **Day 1** — Maven project, `application.properties`, `Company`/`Application` entities.
- **Day 2** — `JpaRepository` interfaces, service-layer interfaces + implementations.
- **Day 3** — `@RestController` CRUD endpoints for companies and applications.
- **Day 4** — Custom JPQL filtering (`findByFilters` — name + minimum CTC) and `@PatchMapping` for status updates.
- **Day 5** — `@Valid` input protection across all DTOs, global exception handling, Postman collection.
- **Day 6** — Springdoc OpenAPI / Swagger UI at `/swagger-ui.html`.
- **Day 7** — Dockerfile, docker-compose, Render blueprint, and this README.
- **Day 8** — JWT authentication: `User` entity, Spring Security, register/login,
  protected `/api/**` routes, and a profile endpoint for the frontend's
  login/profile screens.
