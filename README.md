# Scaffold Calculator

A web application for calculating **Tube & Coupler scaffold materials** for single-storey houses. Built with Java Spring Boot and Thymeleaf, deployed on Render.com.

**Live demo:** https://scaffold-calculator.onrender.com

---

## What it does

The app takes house dimensions and scaffold preferences from a foreman and returns a complete material list — what to load on the lorry to the construction site.

### Inputs
- House shape: **rectangular** or **L-shape** (with cut)
- Length, width, cut dimensions
- Roof type: **Gable / Hip / Flat** (with optional pitch and gable count)
- Up to **4 lifts** with custom heights and per-lift board sizes
- Standard tube size, ledger scenario (TOP / BOTTOM corner pattern)

### Outputs
- **Main components** — standards, ledgers, handrails, transoms, boards, base plates, sole boards, toeboards, advance guard rail sets
- **Fittings** — right-angle, swivel, sleeve, putlog couplers
- **Bracing** — sway bracing, ledger bracing
- **Access towers** — loading bay and ladder tower (per lift count)
- **Per-tube-size breakdown** — how many 5ft, 6ft, 8ft, 10ft, 13ft, 21ft tubes
- **Per-wall breakdown** — which tube combinations cover each wall
- **Per-wall-group board breakdown** — fleet layout for 1 platform
- **PDF export** — full pick list including a top-view diagram with TOP/BOTTOM ledger badges, suitable for the yard operator loading the lorry

### Other features
- Account registration and session-based login (BCrypt password hashes)
- Saved calculation history per user with download-as-PDF
- Role-based access: **USER / MANAGER / ADMIN**
- Admin panel: enable/disable users, change roles
- Live SVG diagram on the calculator form previewing the house with TOP/BOTTOM walls

---

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.3 (server-side MVC) |
| Security | Spring Security (form login + sessions, BCrypt) |
| Database | PostgreSQL (Render-managed in production, local for dev) |
| ORM | Spring Data JPA + Hibernate |
| View | Thymeleaf + Bootstrap 5 + Bootstrap Icons |
| PDF generation | OpenPDF 1.4.2 (LGPL/MPL fork of iText) |
| Build | Maven |
| Boilerplate | Lombok |
| Tests | JUnit 5 + Spring Boot Test (31 unit tests) |
| Deployment | Render.com (auto-deploy on push to `master`) |

JWT classes (`JwtUtil`, `JwtAuthFilter`) are present but **not wired into the security chain** — kept as a base for a future REST API. Current architecture is plain server-side MVC with `HttpSession`.

---

## Architecture (layered)

```
┌─────────────────────────────────────────────────────┐
│  Browser  ───►  Thymeleaf views (templates/*.html)  │
└─────────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│  Controllers  (HTTP / form binding)                 │
│   ├─ AuthController          /login, /register      │
│   ├─ CalculatorController    /calculator            │
│   ├─ HistoryController       /history, /history/all,│
│   │                           /history/{id}/pdf     │
│   ├─ AdminController         /admin/users/*         │
│   └─ GlobalExceptionHandler                         │
└─────────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│  Services  (business logic, transactional)          │
│   ├─ TubeAndCouplerService   ← calculator core      │
│   ├─ GableService            gable scaffold         │
│   ├─ AccessTowerService      loading bay + ladder   │
│   ├─ CalculationService      persist + fetch        │
│   ├─ PdfExportService        OpenPDF generation     │
│   ├─ UserService / AdminService                     │
└─────────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│  Repositories  (Spring Data JPA)                    │
│   ├─ UserRepository                                 │
│   └─ CalculationRepository  (LEFT JOIN FETCH lifts) │
└─────────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│  PostgreSQL                                         │
│   ├─ users                                          │
│   ├─ calculations  (Map fields stored as JSON text) │
│   └─ calculation_lifts                              │
└─────────────────────────────────────────────────────┘
```

---

## Project Structure

```
scaffold-calculator/
├── src/
│   ├── main/
│   │   ├── java/com/scaffold/
│   │   │   ├── ScaffoldCalculatorApplication.java
│   │   │   ├── controller/        # HTTP endpoints (5 files)
│   │   │   ├── service/           # Business logic (7 files)
│   │   │   ├── repository/        # JPA repositories (2 files)
│   │   │   ├── entity/            # JPA entities + JSON converters
│   │   │   ├── model/             # POJO models, enums, utils
│   │   │   ├── dto/               # Request/response DTOs
│   │   │   ├── exception/         # Custom domain exceptions
│   │   │   └── security/          # Spring Security config + JWT skeleton
│   │   └── resources/
│   │       ├── templates/         # Thymeleaf views
│   │       │   ├── admin/         # Admin user management
│   │       │   ├── fragments/     # Shared header/nav
│   │       │   └── *.html         # calculator, result, history, login, register, error
│   │       └── application.properties
│   └── test/
│       └── java/com/scaffold/
│           └── TubeAndCouplerServiceTest.java   # 31 passing unit tests
├── docs/
│   └── diagrams.md                # Architecture and flow diagrams
└── pom.xml
```

---

## Endpoints

The app uses **server-side MVC** (form posts and HTML responses), not a REST API.

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/login` | public | login form |
| GET, POST | `/register` | public | new user signup |
| GET, POST | `/calculator` | authenticated | run a calculation |
| GET | `/history` | authenticated | own past calculations |
| GET | `/history/all` | MANAGER, ADMIN | every user's calculations |
| GET | `/history/{id}/pdf` | owner OR MANAGER, ADMIN | download calculation as PDF |
| GET | `/admin/users` | ADMIN | user management |
| POST | `/admin/users/{id}/role` | ADMIN | change a user's role |
| POST | `/admin/users/{id}/active` | ADMIN | enable/disable a user |

---

## User Roles

- **ROLE_USER** — run calculator, view own history, download own PDFs
- **ROLE_MANAGER** — above + view every calculation, download anyone's PDF
- **ROLE_ADMIN** — everything + assign roles, enable/disable accounts

---

## Running locally

1. Install **Java 17**, **Maven**, **PostgreSQL**.
2. Create a database named `scaffold_db` (or anything — match it in step 3).
3. Update `src/main/resources/application.properties` with your DB credentials. The Render production config uses `DATABASE_URL` from environment.
4. Run from IntelliJ (recommended) or:
   ```
   mvn spring-boot:run
   ```
5. Open http://localhost:8080 — register an account, then upgrade it to ADMIN directly in the DB if you want to test the admin panel.

To run tests:
```
mvn test
```

---

## Deployment

Pushing to `master` on GitHub triggers an auto-deploy on Render.com. The web service uses the Spring Boot fat jar; the database is a managed PostgreSQL instance whose `DATABASE_URL` is wired into the app via environment variables.

---

## Diagrams

See [docs/diagrams.md](docs/diagrams.md) for sequence flows, class diagrams, and the calculator decision tree.
