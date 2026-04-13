# Scaffold Calculator

A web application for calculating **Tube & Coupler scaffold materials** for single-storey houses. Built with Java Spring Boot backend and Thymeleaf frontend.

---

## What it does

- Calculates all scaffold materials (tubes, couplers, boards, bracing) based on house dimensions
- Supports **rectangular** and **L-shape** houses
- Handles up to **4 lifts** with custom heights
- Supports **Gable, Hip, and Flat** roof types (including gable scaffold)
- Two ledger scenarios (how corners are connected)
- Saves calculation history per user
- Role-based access: USER / MANAGER / ADMIN

---

## Tech Stack

| Category | Tool / Library |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.3 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Frontend | Thymeleaf + Bootstrap 5 |
| Build tool | Maven |
| Boilerplate reduction | Lombok |
| Testing | JUnit 5 + Spring Boot Test |
| IDE | IntelliJ IDEA |
| Version control | Git + GitHub |

---

## Project Structure

```
scaffold-calculator/
├── src/
│   ├── main/
│   │   ├── java/com/scaffold/
│   │   │   ├── controller/       # HTTP endpoints
│   │   │   ├── service/          # Business logic (calculator, gable, auth)
│   │   │   ├── repository/       # Database access
│   │   │   ├── entity/           # DB table classes
│   │   │   ├── model/            # Input/output models, enums
│   │   │   ├── dto/              # Data transfer objects
│   │   │   └── security/         # JWT filter, config
│   │   └── resources/
│   │       ├── templates/        # Thymeleaf HTML pages
│   │       └── application.properties
│   └── test/
│       └── java/com/scaffold/
│           ├── CalculatorTest.java
│           └── TubeAndCouplerServiceTest.java
├── docs/
│   └── diagrams.md               # Architecture diagrams
├── diagrams.html                 # Diagrams (local browser view)
└── pom.xml
```

---

## API Endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/calculator/calculate` | ROLE_USER+ |
| GET | `/api/history/me` | ROLE_USER+ |
| GET | `/api/history/all` | ROLE_MANAGER+ |
| GET | `/api/admin/users` | ROLE_ADMIN |
| PUT | `/api/admin/users/{id}/role` | ROLE_ADMIN |
| PUT | `/api/admin/users/{id}/active` | ROLE_ADMIN |

---

## User Roles

- **ROLE_USER** — run calculator, view own history
- **ROLE_MANAGER** — above + view all calculations
- **ROLE_ADMIN** — everything + manage users and roles

---

## Running locally

1. Install Java 17, PostgreSQL, Maven
2. Create a PostgreSQL database named `scaffold_db`
3. Update `src/main/resources/application.properties` with your DB credentials
4. Run: `mvn spring-boot:run`
5. Open: `http://localhost:8080`

---

## Diagrams

See [docs/diagrams.md](docs/diagrams.md) for full architecture, flow, and database diagrams.
