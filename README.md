# Finance Dashboard — Backend

A role-based finance management backend built with
Java 21, Spring Boot 3.4, H2, and JWT.

---

## Tech Stack

| Layer        | Technology                     |
|--------------|--------------------------------|
| Language     | Java 21                        |
| Framework    | Spring Boot 3.4.1              |
| Database     | H2 (in-memory)                 |
| Security     | Spring Security + JWT (jjwt)   |
| Docs         | SpringDoc OpenAPI (Swagger UI) |
| Tests        | JUnit 5 + Mockito              |
| Build        | Maven                          |

---

## Quick Start
```bash
git clone https://github.com/m-vetrivel/Finance-Data-Processing-and-Access-Control-Backend.git
cd Finance-Data-Processing-and-Access-Control-Backend
mvn spring-boot:run
```

App runs on **http://localhost:8080**

---

## Default Users (seeded on startup)

| Username | Password   | Role    |
|----------|------------|---------|
| admin    | admin123   | ADMIN   |
| analyst  | analyst123 | ANALYST |
| viewer   | viewer123  | VIEWER  |

---

## URLs

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/h2-console | H2 DB console |
| http://localhost:8080/api-docs | Raw OpenAPI JSON |

**H2 Console settings:**
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa` — Password: _(empty)_

---

## Role Access Matrix

| Action                        | VIEWER | ANALYST | ADMIN |
|-------------------------------|--------|---------|-------|
| Login                         | ✅     | ✅      | ✅    |
| View records                  | ✅     | ✅      | ✅    |
| View dashboard summary        | ✅     | ✅      | ✅    |
| Create / update records       | ❌     | ✅      | ✅    |
| Delete records (soft)         | ❌     | ❌      | ✅    |
| Manage users                  | ❌     | ❌      | ✅    |

---

## API Overview

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Get JWT token |
| POST | `/api/auth/register` | Register new user |

### Financial Records
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/records` | List records (paginated + filtered) |
| GET | `/api/records/{id}` | Get single record |
| POST | `/api/records` | Create record |
| PUT | `/api/records/{id}` | Update record |
| DELETE | `/api/records/{id}` | Soft delete record |

**Filter params for GET /api/records:**
```
?type=INCOME&category=SALARY&from=2024-01-01&to=2024-12-31
&page=0&size=10&sortBy=date&direction=desc
```

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/summary` | Full summary |
```
?trendMonths=6   ← how many months of trend data
```

### Users (ADMIN only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user |
| PUT | `/api/users/{id}` | Update role/status |
| DELETE | `/api/users/{id}` | Delete user |

---

## Project Structure
```
src/main/java/com/finance/dashboard/
├── config/       SecurityConfig, SwaggerConfig, DataSeeder
├── controller/   Auth, User, FinancialRecord, Dashboard
├── dto/          Request/Response records
├── entity/       User, FinancialRecord
├── enums/        Role, TransactionType, Category
├── exception/    AppException, GlobalExceptionHandler
├── repository/   JPA repositories with JPQL queries
├── security/     JwtUtils, JwtAuthFilter, UserDetailsServiceImpl
└── service/      Auth, User, FinancialRecord, Dashboard
```

---

## Design Decisions & Assumptions

- **H2 in-memory** — data resets on restart; swap datasource
  config for PostgreSQL/MySQL with no code changes needed
- **Soft delete** — records are flagged `deleted=true`,
  never physically removed; supports audit trail
- **JWT stateless** — no sessions; token expiry is 24 hours
- **Role hierarchy** — enforced at both URL and method level
  via `SecurityConfig` + `@PreAuthorize`
- **`/api/auth/register` is open** — in production this
  should be ADMIN-only or removed; left open for easy setup
- **BigDecimal for money** — avoids floating-point precision
  issues with financial data

---

## Running Tests
```bash
mvn test
```
