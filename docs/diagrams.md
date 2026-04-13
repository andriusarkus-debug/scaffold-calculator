# Scaffold Calculator — Architecture Diagrams

---

## 1. System Overview

```mermaid
graph TD
    subgraph Clients
        WEB[Web Browser\nHTML/CSS/JS]
        MOB[Mobile App\nReact Native / Flutter]
    end

    subgraph Backend["Java Spring Boot API (port 8080)"]
        AUTH[Auth Layer\nJWT + Spring Security]
        CTRL[Controllers]
        SVC[Services\nBusiness Logic]
        REPO[Repositories\nSpring Data JPA]
    end

    subgraph DB["PostgreSQL"]
        USERS[(users)]
        CALCS[(calculations)]
        LIFTS[(calculation_lifts)]
    end

    WEB -- "HTTP REST / JSON" --> AUTH
    MOB -- "HTTP REST / JSON" --> AUTH
    AUTH --> CTRL
    CTRL --> SVC
    SVC --> REPO
    REPO --> USERS
    REPO --> CALCS
    REPO --> LIFTS
```

---

## 2. Layer Architecture

```mermaid
graph LR
    subgraph Controllers
        AC[AuthController]
        CC[CalculatorController]
        HC[HistoryController]
        ADC[AdminController]
    end

    subgraph Services
        US[UserService]
        TC[TubeAndCouplerService]
        GS[GableService]
        CS[CalculationService]
        AS[AdminService]
    end

    subgraph Repositories
        UR[UserRepository]
        CR[CalculationRepository]
    end

    subgraph Database
        PG[(PostgreSQL)]
    end

    AC --> US
    CC --> TC
    CC --> CS
    TC --> GS
    HC --> CS
    ADC --> AS
    AS --> UR
    US --> UR
    CS --> CR
    UR --> PG
    CR --> PG
```

---

## 3. JWT Authentication Flow

```mermaid
sequenceDiagram
    actor User
    participant API as Spring Boot API
    participant SEC as JwtAuthFilter
    participant DB as PostgreSQL

    User->>API: POST /api/auth/login {username, password}
    API->>DB: Find user by username
    DB-->>API: User (passwordHash, role, active)
    API->>API: BCrypt check
    API->>API: Generate JWT (username + role, 24h)
    API-->>User: { token: "eyJ..." }

    Note over User,API: Every subsequent request

    User->>SEC: GET /api/history/me\nAuthorization: Bearer eyJ...
    SEC->>SEC: Validate JWT signature and expiry
    SEC->>SEC: Extract username + role
    SEC->>API: Forward request with user context
    API-->>User: 200 OK + data
```

---

## 4. Calculation Flow

```mermaid
sequenceDiagram
    actor User
    participant CTRL as CalculatorController
    participant TC as TubeAndCouplerService
    participant GS as GableService
    participant CS as CalculationService
    participant DB as PostgreSQL

    User->>CTRL: POST /api/calculator/calculate { ScaffoldInput }
    CTRL->>TC: calculate(input)

    alt RECTANGULAR
        TC->>TC: 4 faces, returnCount=4
    else L_SHAPE
        TC->>TC: 6 faces, returnCount=5\n(corner D has no return)
    end

    TC->>TC: Bays = perimeter / 1.8m
    TC->>TC: Standards, Ledgers, Transoms
    TC->>TC: Boards (working lift only)
    TC->>TC: Bracing (sway + ledger)
    TC->>TC: Couplers (all types)
    TC->>TC: Tube combinations (greedy algorithm)

    alt RoofType = GABLE
        TC->>GS: calculateGable(input)
        GS-->>TC: gable materials
    end

    TC-->>CTRL: MaterialResult

    CTRL->>CS: save(input, result, user)
    CS->>DB: INSERT calculations + lifts
    DB-->>CS: OK

    CTRL-->>User: 200 OK { MaterialResult }
```

---

## 5. Role & Access Control

```mermaid
graph TD
    subgraph Endpoints
        E1["POST /api/auth/register\nPOST /api/auth/login"]
        E2["POST /api/calculator/calculate\nGET /api/history/me"]
        E3["GET /api/history/all"]
        E4["GET /api/admin/users\nPUT /api/admin/users/id/role\nPUT /api/admin/users/id/active"]
    end

    subgraph Roles
        PUB[Public\nno token]
        USER[ROLE_USER]
        MGR[ROLE_MANAGER]
        ADM[ROLE_ADMIN]
    end

    PUB --> E1
    USER --> E2
    MGR --> E2
    MGR --> E3
    ADM --> E2
    ADM --> E3
    ADM --> E4
```

---

## 6. Database Schema

```mermaid
erDiagram
    users {
        bigint id PK
        varchar username
        varchar email
        varchar password_hash
        varchar role
        boolean active
        timestamp created_at
    }

    calculations {
        bigint id PK
        bigint user_id FK
        varchar project_name
        varchar house_shape
        double house_length
        double house_width
        varchar roof_type
        int lift_count
        int bays
        double perimeter
        text material_results
        timestamp created_at
    }

    calculation_lifts {
        bigint id PK
        bigint calculation_id FK
        int lift_number
        double height
        boolean has_boards
    }

    users ||--o{ calculations : "has"
    calculations ||--o{ calculation_lifts : "has"
```
