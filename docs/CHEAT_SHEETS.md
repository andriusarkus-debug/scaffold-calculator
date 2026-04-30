# Scaffold Calculator — Package Cheat Sheets

> **Pristatymo paruoštukė** — vienas "lapas" per package'ą, su pagrindiniais akcentais ir galimais mokytojo klausimais.
>
> Tikslas: po šio dokumento turi galėti atsakyti **savais žodžiais** apie kiekvieną package'ą.

---

## 📋 Turinys

1. [Architektūros visuma](#0-architektūros-visuma)
2. [`controller/` — HTTP įvestis/išvestis](#1-controller)
3. [`service/` — verslo logika](#2-service)
4. [`repository/` — DB prieiga](#3-repository)
5. [`entity/` — DB lentelių atvaizdas](#4-entity)
6. [`model/` — POJO duomenų klasės](#5-model)
7. [`dto/` — duomenų perdavimas](#6-dto)
8. [`exception/` — domeno klaidos](#7-exception)
9. [`security/` — autentifikacija](#8-security)

---

## 0. Architektūros visuma

### Layered (sluoksniuota) architektūra

```
Browser  →  Controller  →  Service  →  Repository  →  PostgreSQL
                ↑
             (Thymeleaf templates)
```

**Kiekvienas sluoksnis turi vieną atsakomybę** (Single Responsibility Principle):

| Sluoksnis | Atsakomybė |
|---|---|
| **Controller** | HTTP requestai/responsai, formų bindingas |
| **Service** | Verslo logika, transakcijos, skaičiavimai |
| **Repository** | DB prieiga (per JPA) |
| **Entity** | DB lentelių atvaizdas |

### Design pattern'ai

- **Layered architecture** — sluoksniavimas
- **Dependency Injection** — `@RequiredArgsConstructor` + `final` laukai
- **Builder pattern** — Lombok `@Builder` (User, Calculation)
- **DTO pattern** — atskiros klasės API įvestims/išvestims

### 🎯 Pristatymui

> *"Mano projektas naudoja klasikinę **layered architecture**: Controller'is gauna HTTP request'ą, deleguoja Service'ui, Service kreipiasi į Repository, Repository kalbasi su PostgreSQL per JPA. Kiekvienas sluoksnis turi aiškų vaidmenį — tai daro kodą lengvai testuojamą ir keičiamą."*

---

## 1. `controller/`

### Kas yra?
HTTP endpoint'ai — prima requestus iš naršyklės, grąžina HTML (per Thymeleaf) arba PDF failą.

### Failai (5)

| Failas | Endpoint'ai |
|---|---|
| **AuthController** | `/login`, `/register` |
| **CalculatorController** | `/calculator` (GET + POST) |
| **HistoryController** | `/history`, `/history/all`, `/history/{id}/pdf` |
| **AdminController** | `/admin/users/{id}/role`, `/admin/users/{id}/active` |
| **GlobalExceptionHandler** | Centralizuotas klaidų valdymas |

### 🔑 Pagrindiniai akcentai

- **`@Controller`** (ne `@RestController`) — grąžina HTML view pavadinimus, ne JSON
- **`@RequiredArgsConstructor`** — Lombok'as, automatinis konstruktorius su `final` laukais (Spring DI)
- **`@AuthenticationPrincipal User currentUser`** — Spring Security automatiškai įdeda prisijungusį vartotoją
- **`Model`** — Spring objektas, į kurį dedam atributus Thymeleaf'ui
- **`@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")`** — autorizacija lygmenyje metodo

### ⚠️ Mokytojo klausimai

**K:** *Kuo skiriasi `@Controller` nuo `@RestController`?*
**A:** `@Controller` grąžina view name (Thymeleaf template), `@RestController` grąžina JSON (REST API). Mano projektas — server-side MVC, todėl `@Controller`.

**K:** *Kaip Spring žino, kuriuo vartotoju dabar prisijungta?*
**A:** Per `@AuthenticationPrincipal` arba `Principal principal`. Spring Security saugo SecurityContext per Session.

### 🧠 Mnemonic'as
> **„Controller = recepcionistas: pasitinka klientą, paklausia ko nori, perduoda specialistui (Service'ui)."**

---

## 2. `service/`

### Kas yra?
Verslo logika — visi skaičiavimai, taisyklės, transakcijos.

### Failai (7)

| Failas | Vaidmuo |
|---|---|
| **TubeAndCouplerService** | 🫀 ŠIRDIS — apskaičiuoja visus scaffold materialus |
| **GableService** | Gable scaffold (frontonai) |
| **AccessTowerService** | Loading bay + ladder tower |
| **CalculationService** | Išsaugo / pakrauna skaičiavimus iš DB |
| **PdfExportService** | Generuoja PDF (OpenPDF) |
| **UserService** | Vartotojų registracija, paieška |
| **AdminService** | Vaidmenų ir aktyvumo keitimas |

### 🔑 Pagrindiniai akcentai

- **`@Service`** — Spring stereotype, sukuria bean'ą
- **`@Transactional`** — atomiškumas (jei klaida — rollback)
- **`@Transactional(readOnly = true)`** klasės lygyje — visi metodai read-only pagal nutylėjimą
- **`final` laukai + `@RequiredArgsConstructor`** — DI per konstruktorių
- **POJO modeliai** kaip įvestis/išvestis (`ScaffoldInput`, `MaterialResult`)

### ⚠️ Mokytojo klausimai

**K:** *Kodėl skaičiavimo logika ne Controller'yje?*
**A:** Single Responsibility — Controller atsakingas tik už HTTP, Service už verslo logiką. Service galima testuoti **be** HTTP infrastruktūros.

**K:** *Kas yra `@Transactional`?*
**A:** Anotacija, kuri apvelka metodą DB transakcijoje. Jei metode įvyksta klaida — visi DB pakeitimai atšaukiami (rollback).

### 🧠 Mnemonic'as
> **„Service = specialistas: žino taisykles, apskaičiuoja, sako, ką daryti."**

---

## 3. `repository/`

### Kas yra?
DB prieigos sluoksnis. Naudoja Spring Data JPA — užklausos generuojamos automatiškai iš metodo pavadinimo.

### Failai (2)

| Failas | Metodai |
|---|---|
| **UserRepository** | `findByUsername`, `findByEmail`, `existsByUsername`, `existsByEmail` |
| **CalculationRepository** | `findByUserIdOrderByCreatedAtDesc`, `findAllByOrderByCreatedAtDesc`, `findByIdWithLiftsAndUser` |

### 🔑 Pagrindiniai akcentai

- **`extends JpaRepository<Entity, Long>`** — automatiškai gaunam `save`, `findAll`, `findById`, `delete`
- **Method name → SQL** — `findByUsername` → `SELECT * FROM users WHERE username = ?`
- **`@Query`** — kai reikia custom SQL (pvz. `LEFT JOIN FETCH` lazy kolekcijai)
- **N+1 problema** — sprendžiama per `JOIN FETCH`

### Pavyzdys
```java
@Query("SELECT c FROM Calculation c LEFT JOIN FETCH c.lifts WHERE c.user.id = :userId")
List<Calculation> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
```
*(Vienas SQL su JOIN, ne N+1 atskirų užklausų liftams)*

### ⚠️ Mokytojo klausimai

**K:** *Kas yra N+1 problema?*
**A:** Kai pirmas SELECT grąžina N įrašų, ir kiekvienam įrašui Hibernate daro atskirą SELECT lazy laukui — iš viso 1+N užklausų. Sprendimas: `JOIN FETCH`.

**K:** *Kaip Spring žino, kaip generuoti SQL iš metodo pavadinimo?*
**A:** Method name parsing'as — Spring Data JPA analizuoja pavadinimą (pvz. `findByUsernameAndActive`) ir sukuria atitinkamą JPQL.

### 🧠 Mnemonic'as
> **„Repository = bibliotekininkas: žino, kur lentyna, kaip rasti, kaip sutvarkyti."**

---

## 4. `entity/`

### Kas yra?
DB lentelių atvaizdas Java pavidalu. JPA/Hibernate konvertuoja į abi puses.

### Failai (4 + 2 converters)

```
entity/
├── User.java           ← users lentelė
├── Role.java           ← enum
├── Calculation.java    ← calculations lentelė
├── CalculationLift.java← calculation_lifts lentelė
└── converter/
    ├── MapToJsonConverter.java       (Map<String, Integer> → JSON)
    └── MapStringToJsonConverter.java (Map<String, String>  → JSON)
```

### 🔑 Pagrindiniai akcentai

| Anotacija | Reikšmė |
|---|---|
| `@Entity` | JPA žymeklis — ši klasė yra DB lentelė |
| `@Table(name = "users")` | Eksplicitinis lentelės pavadinimas (`user` yra reserved PostgreSQL'e!) |
| `@Id` + `@GeneratedValue` | Primary key + auto-increment |
| `@Column(unique=true, nullable=false)` | Constraintas DB lygmenyje |
| `@Enumerated(EnumType.STRING)` | Enum saugomas kaip TEKSTAS, ne skaičius (refactor-safe!) |
| `@Builder.Default` | **Lombok** — priverčia `@Builder` gerbti `= true` field initializer |
| `@PrePersist` | Lifecycle callback — vykdomas prieš pirmą INSERT |
| `@ManyToOne(fetch = LAZY)` | Daug-į-vieną santykis (User ↔ Calculation) |
| `@OneToMany(mappedBy = "...", cascade = ALL)` | Vienas-į-daug (Calculation ↔ Lifts) |
| `@Convert(converter = ...)` | Custom konvertavimas (Map ↔ JSON) |

### Santykių diagrama

```
User  1───N  Calculation  1───N  CalculationLift
                    │
                    └─ Map fields → JSON via @Converter
```

### Kodėl ne `@Data`?

**Dvi priežastys (svarbu!):**
1. 🔧 **Korektiškumas** — `@Data` generuoja `equals/hashCode` pagal visus laukus → JPA + lazy kolekcijos = `LazyInitializationException`. Vietoj to: rankomis pagal `id`.
2. 🔒 **Saugumas** — `@Data` sugeneruotas `toString()` parodytų `passwordHash`. Vietoj to: `@ToString(exclude = "passwordHash")`.

### ⚠️ Mokytojo klausimai

**K:** *Kas atsitiktų be `@Enumerated(EnumType.STRING)`?*
**A:** Default `ORDINAL` — saugo enum'o **poziciją**. Jei pridėtum naują reikšmę vidury enum'o, visos esamos DB eilutės būtų neteisingai interpretuojamos. STRING — refactor-safe.

**K:** *Kodėl `@Builder.Default`?*
**A:** **Lombok** anotacija. Be jos `@Builder` ignoruoja `private boolean active = true;` ir builder grąžintų `false`. `@Builder.Default` priverčia gerbti default'ą.

**K:** *Kodėl Map laukas turi `@Convert`?*
**A:** PostgreSQL neturi `Map` tipo. Custom konverteris paverčia `Map<String, Integer>` į JSON tekstą prieš įrašant.

### 🧠 Mnemonic'as
> **„Entity = lentelės atspindis Java veidrodyje."**

---

## 5. `model/`

### Kas yra?
**Plain POJO** klasės (Plain Old Java Objects) — tik laukai + getter/setter, **be JPA anotacijų**. Naudojamos kaip įvestys/išvestys tarp sluoksnių.

### Failai

```
model/
├── ScaffoldInput.java      ← formos įvestis
├── LiftInput.java          ← vienas liftas formoje
├── MaterialResult.java     ← skaičiavimo rezultatas
├── LoadingBayResult.java   ← loading bay rezultatas
├── LadderTowerResult.java  ← ladder tower rezultatas
├── ScaffoldConstants.java  ← konstantos (BAY_SPACING ir t.t.)
├── TubeDeliveryUtil.java   ← konsolidavimo helperis
└── enums/
    ├── HouseShape.java       (RECTANGULAR, L_SHAPE)
    ├── RoofType.java         (GABLE, HIP, FLAT)
    ├── TubeSize.java         (FIVE_FOOT, ..., TWENTY_ONE_FOOT)
    ├── BoardSize.java        (panašiai)
    └── LedgerScenario.java   (SCENARIO_ONE, SCENARIO_TWO)
```

### 🔑 Pagrindiniai akcentai

- **Be JPA anotacijų** — paprasti POJO
- **`@Data`** Lombok — čia saugu (ne JPA entity, lazy kolekcijų nėra)
- **`@Builder`** — patogus kūrimui
- **Enum'ai** atskirame subpackage'e — domeno reikšmės
- **Anemic Domain Model** — modeliai laiko duomenis, logika gyvena Service'uose

### ⚠️ Mokytojo klausimai

**K:** *Kuo skiriasi `model/` nuo `entity/`?*
**A:** **`entity/`** — JPA klasės, atvaizduoja DB lenteles, turi anotacijas (`@Entity`, `@Column`...). **`model/`** — paprasti POJO, neperšistuojami DB, naudojami formų įvestims ir skaičiavimų rezultatams perduoti tarp sluoksnių.

**K:** *Kodėl naudoji `@Data` tik `model/`, ne `entity/`?*
**A:** `model/` klasėse nėra lazy kolekcijų ir nėra `passwordHash`, todėl `@Data` saugu. `entity/` — kitaip.

### 🧠 Mnemonic'as
> **„Model = duomenų krepšelis tarp sluoksnių, be ryšio su DB."**

---

## 6. `dto/`

### Kas yra?
**Data Transfer Object** — atskiros klasės API įvestims (request) ir išvestims (response).

### Failai

```
dto/
├── LoginRequest.java       ← username, password
├── RegisterRequest.java    ← username, email, password, ...
├── AuthResponse.java       ← token, user info
├── UserDto.java            ← user be passwordHash (saugus formatas)
├── RoleUpdateRequest.java  ← admin keičia rolę
└── ActiveUpdateRequest.java← admin keičia aktyvumą
```

### 🔑 Pagrindiniai akcentai

- **Saugumas** — `UserDto` neturi `passwordHash`, kad nepatektų į API atsakymą
- **API stabilumas** — pakeitus DB schemą, DTO nesikeičia
- **Validacija** — `@NotBlank`, `@Email`, `@Size(...)` ant DTO laukų
- **Atskira klasė request'ui ir response'ui** — net jei laukai panašūs

### Pavyzdys
```java
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
    // ❌ NĖRA passwordHash!
}
```

### ⚠️ Mokytojo klausimai

**K:** *Kodėl negrąžini `User` entity tiesiai į API?*
**A:** Saugumas (slaptažodžio hash leak'as), stabilumas (DB pakeitimai paveiktų API), kontrolė (galim slėpti vidinius laukus).

**K:** *Kuo DTO skiriasi nuo Entity?*
**A:** Entity — DB modelis, DTO — komunikacijos modelis. Skirtingos atsakomybės, dažnai skirtingi laukai.

### 🧠 Mnemonic'as
> **„DTO = pakavimas siuntinio, kurį siunčiame klientui — tik tai, ką jis turi matyti."**

---

## 7. `exception/`

### Kas yra?
Custom domain exceptions — verslo logikos klaidų hierarchija.

### Failai

```
exception/
├── DomainException.java         ← abstract base (extends RuntimeException)
├── UserNotFoundException.java
├── UsernameTakenException.java
├── EmailTakenException.java
└── AccountDisabledException.java
```

### 🔑 Pagrindiniai akcentai

- **`extends RuntimeException`** — unchecked exception (nereikia `throws`)
- **Hierarchija** — visas extends `DomainException`, kad lengva gaudyti vienoje vietoje
- **`GlobalExceptionHandler`** controller'yje gaudo ir paverčia į vartotojui aiškią klaidą
- **Žinutės lietuviškai** — vartotojo kalba

### Pavyzdys
```java
public class UsernameTakenException extends DomainException {
    public UsernameTakenException(String username) {
        super("Vartotojas '" + username + "' jau egzistuoja.");
    }
}
```

### ⚠️ Mokytojo klausimai

**K:** *Kodėl custom exceptions, o ne `IllegalArgumentException`?*
**A:** **Aiškumas** — `UsernameTakenException` iš karto sako, kas atsitiko. **Hierarchija** — galiu sugauti visas domeno klaidas viena `catch (DomainException e)`. **Atskyrimas** — domeno klaidos atskirtos nuo techninių (NullPointer, ir t.t.).

**K:** *Kodėl `RuntimeException`, ne `Exception`?*
**A:** Spring rekomenduoja unchecked exceptions verslo logikos klaidoms. Mažiau boilerplate kodo (be `throws` kiekviename signature'e).

### 🧠 Mnemonic'as
> **„Exception = aiškus signalas, kad kažkas verslo prasme nutiko."**

---

## 8. `security/`

### Kas yra?
Spring Security konfigūracija + custom logika.

### Failai (4)

| Failas | Vaidmuo |
|---|---|
| **SecurityConfig** | Security chain konfigūracija (filtrai, autorizacijos taisyklės) |
| **CustomUserDetailsService** | Pakraunam User iš DB pagal username (Spring Security reikalauja) |
| **JwtUtil** | JWT token kūrimas/validavimas (NENAUDOJAMA — paliktas ateičiai) |
| **JwtAuthFilter** | JWT filtras (NENAUDOJAMA) |

### 🔑 Pagrindiniai akcentai

- **Form login + sessions** — server-side MVC autentifikacija
- **BCrypt** — slaptažodžio hash'as (one-way, salt'ed)
- **`UserDetailsService`** — Spring Security plug-in pointas; mes implementuojam savo
- **Roles per `@PreAuthorize`** — autorizacija metodų lygyje
- **CSRF apsauga** įjungta (kitaip POST'ai dirbti negalėtų)
- **JWT klasės** — paliktos kaip evolution path į REST API ateityje (YAGNI principas — nedarom dabar, bet paruoštos)

### Auth flow

```
1. POST /login (username + password)
        ↓
2. Spring Security → CustomUserDetailsService.loadUserByUsername()
        ↓
3. UserRepository.findByUsername() → User objektas
        ↓
4. Spring Security: BCrypt.matches(password, user.passwordHash)
        ↓
5. ✓ → Sukuria SecurityContext + Session, redirect į /calculator
   ✗ → Klaidos pranešimas
```

### ⚠️ Mokytojo klausimai

**K:** *Kodėl BCrypt, ne MD5/SHA?*
**A:** BCrypt yra **slow-by-design** ir naudoja **salt** automatiškai. MD5/SHA per greiti — brute-force atakos lengvos. BCrypt'ui kiekvienas hash užtrunka ~100ms, brute-force tampa neįmanomas.

**K:** *Kuo skiriasi sesijos nuo JWT?*
**A:** **Sesijos** — server'is laiko būseną (kas prisijungęs), klientas turi tik session ID cookie. **JWT** — visa info encoded token'e, server stateless. Mano projekte — sesijos (paprasčiau MVC kontekste).

**K:** *Kas yra CSRF ir kodėl reikia apsaugos?*
**A:** Cross-Site Request Forgery — kenkėjiška svetainė priverčia tavo naršyklę siųsti POST į kitą svetainę, kur esi prisijungęs. Spring Security automatiškai prideda CSRF token'ą į formas, ir tikrina jo buvimą.

### 🧠 Mnemonic'as
> **„Security = pasų kontrolė: tikrina kas tu, ką gali daryti, ir kad nieks už tave to nepadarytų."**

---

## 🎓 Bendra atmintinė pristatymui

### Architektūros sakinys
> *"Spring Boot 3 server-side MVC aplikacija su layered architecture (Controller → Service → Repository), Thymeleaf view'ais, PostgreSQL per JPA, Spring Security su sesijomis ir BCrypt'u, OpenPDF eksportui."*

### 4 design pattern'ai, kuriuos minėsiu
1. **Layered architecture** (sluoksniai)
2. **Dependency Injection** (`@RequiredArgsConstructor`)
3. **Builder pattern** (Lombok `@Builder`)
4. **DTO pattern** (atskiras formatas API)

### 5 dalykai, kurie parodys gilesnį supratimą
1. *„`@Data` JPA entitetams pavojinga — equals/hashCode + lazy kolekcijos = LazyInitializationException."*
2. *„`@Enumerated(EnumType.STRING)` — refactor-safe vietoj ORDINAL."*
3. *„`@Builder.Default` priverčia Lombok gerbti `= true` initializer'ius."*
4. *„BCrypt yra slow-by-design su automatiniu salt'u — saugiau už MD5."*
5. *„JWT klasės paliktos ateičiai — YAGNI principas."*

---

## 📌 Patarimai pristatymo dieną

1. **Pradėk nuo architektūros diagramos** — vizualiai aiškiau
2. **Pavyzdžiai konkretūs** — ne *„saugau vartotoją"*, o *„saugau vartotoją su BCrypt hash'u 60 simbolių ilgio"*
3. **Trade-offs** — minėk ką **NE** darei ir kodėl (REST API ne, JWT ne — paaiškink kodėl tinka MVC + sesijos)
4. **Demo scenarijus** — pasiruošk gyvai parodymui (5×5m + hip → 160 right-angle couplers)
5. **Klausimai mokytojui** pabaigoje — parodysi smalsumą ir savikritiškumą

---

**Sėkmės pristatyme! 🚀**

*Generated: 2026-04-27 — visi pakeitimai iki commit'o `d8665b5`*
