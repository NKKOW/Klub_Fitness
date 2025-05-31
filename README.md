# KlubFitness

## Opis projektu

KlubFitness to aplikacja do zarządzania klubem fitness napisana w Spring Boot. Umożliwia trzem typom użytkowników (USER, TRAINER, ADMIN) zarządzanie użytkownikami, trenerami, sesjami treningowymi oraz rezerwacjami z mechanizmem zniżek opartym na wzorcu Strategy. W projekcie zastosowano zasady SOLID, wzorce projektowe oraz konteneryzację z Dockerem.

## Technologie

* **Język**: Java 21
* **Framework**: Spring Boot (Web, Data JPA, Security, Actuator)
* **ORM**: Hibernate
* **Baza danych**: PostgreSQL
* **Migracje**: Flyway
* **Konteneryzacja**: Docker, Docker Compose
* **Build & Dependency Management**: Maven
* **Testy**: JUnit 5, Testcontainers, Mockito
* **Dokumentacja API**: Springdoc OpenAPI (Swagger UI)
* **Pokrycie testów**: JaCoCo

## Uruchomienie (Docker)

1. Zbuduj i uruchom usługi:

   ```bash
   docker-compose up --build
   ```
2. Aplikacja dostępna pod: `http://localhost:8081`
3. Baza PostgreSQL:

   * Port: 5432
   * Nazwa bazy: `klub_fitness`
   * Użytkownik: `fitnesiara`
   * Hasło: `klubfitness`

## Struktura projektu

```
src/
├─ main/
│  ├─ java/org/example/klubfitness/
│  │  ├─ controller/      # REST API (User, Trainer, Session, Reservation)
│  │  ├─ service/         # Logika biznesowa
│  │  ├─ repository/      # Spring Data JPA
│  │  ├─ entity/          # Encje JPA
│  │  ├─ dto/             # Obiekty DTO
│  │  ├─ security/        # Spring Security, CustomUserDetailsService
│  │  └─ util/strategy/   # Pattern Strategy dla zniżek
│  └─ resources/
│     ├─ application.properties # konfiguracja
│     └─ db/migration/   # skrypty Flyway (V1__init.sql)
└─ test/                 # Unit i integration tests

Dockerfile
`docker-compose.yml`
`pom.xml`
```

## Architektura i wzorce projektowe

* **Controllers** – obsługa żądań HTTP
* **Services** – logika biznesowa
* **Repositories** – dostęp do danych (Spring Data JPA)
* **Entities & DTOs** – model domenowy i transfer danych

### Zastosowane wzorce

* **Repository Pattern** – abstrakcja dostępu do bazy
* **Dependency Injection** – zarządzanie zależnościami przez Spring
* **Strategy** – `DiscountStrategy` i implementacje
* **Builder** – tworzenie `UserDetails` w `CustomUserDetailsService`
* **Polimorfizm** – implementacje strategii i serwisów

### Zasady SOLID

* **Single Responsibility**: każda klasa ma jedno zadanie
* **Open/Closed**: rozszerzalność poprzez implementację strategii
* **Liskov Substitution**: zgodność implementacji Strategy
* **Interface Segregation**: wąskie interfejsy, np. `DiscountStrategy`
* **Dependency Inversion**: zależność od abstrakcji, nie implementacji

## Baza danych i diagram ERD

![image alt](https://github.com/NKKOW/Klub_Fitness/blob/3ade7f13edfb86cfe604d0f886784b9e191b32df/DiagramERD.png)

1. **Tabela `users`** (encja `User`):

   * `id` (serial, klucz główny)
   * `username` (varchar(255), unikatowe, niepuste)
   * `password` (varchar(255), niepuste)
   * `role` (varchar(50), niepuste)

   Użytkownicy mają przypisaną rolę bezpośrednio w tej tabeli, co upraszcza relacje (brak tabeli połączeniowej `user_roles`). Zobacz implementację encji w `User.java` (linia \~10–20).

2. **Tabela `trainers`** (encja `Trainer`):

   * `id` (serial, klucz główny)
   * `name` (varchar(255), niepuste)
   * `specialization` (varchar(255))

   Każdy trener może mieć wiele sesji (związek 1–do–wielu). Implementacja relacji w `Trainer.java` (linia \~20–25).

3. **Tabela `training_sessions`** (encja `TrainingSession`):

   * `id` (serial, klucz główny)
   * `title` (varchar(255), niepuste)
   * `description` (text)
   * `start_time` (timestamp without time zone, niepuste)
   * `end_time` (timestamp without time zone, niepuste)
   * `trainer_id` (integer, niepuste, klucz obcy → `trainers.id`)

   Relacja: wiele sesji należy do jednego trenera. Zobacz w `TrainingSession.java` (linia \~15–25) adnotację `@ManyToOne`.

4. **Tabela `reservations`** (encja `Reservation`):

   * `id` (serial, klucz główny)
   * `reservation_time` (timestamp without time zone, niepuste)
   * `user_id` (integer, niepuste, klucz obcy → `users.id`)
   * `session_id` (integer, niepuste, klucz obcy → `training_sessions.id`)

   Każda rezerwacja wiąże użytkownika z sesją. Relacje łączą tę tabelę z `users` i `training_sessions`. Implementacja w `Reservation.java` (linia \~5–15).

## Migracje bazy danych

Skrypty Flyway w `src/main/resources/db/migration/V1__init.sql` tworzą tabele:

* `users`, `trainers`, `training_sessions`, `reservations`

## Dokumentacja API (Swagger UI)

Interaktywną dokumentację udostępnia:

```
http://localhost:8081/swagger-ui.html
```
![image alt](https://github.com/NKKOW/Klub_Fitness/blob/3ade7f13edfb86cfe604d0f886784b9e191b32df/Swagger1.png)
![image alt](https://github.com/NKKOW/Klub_Fitness/blob/3ade7f13edfb86cfe604d0f886784b9e191b32df/Swagger2.png)
![image alt](https://github.com/NKKOW/Klub_Fitness/blob/3ade7f13edfb86cfe604d0f886784b9e191b32df/Swagger3.png)

## Obsługiwane endpointy

### Użytkownicy (`/api/users`)

* `GET`    `/api/users`              – lista użytkowników (ADMIN)
* `POST`   `/api/users`              – tworzenie (ADMIN)
* `GET`    `/api/users/{id}`         – pobierz po ID (ADMIN)
* `PUT`    `/api/users/{id}`         – aktualizacja (ADMIN)
* `DELETE` `/api/users/{id}`         – usuwanie (ADMIN)

### Trenerzy (`/api/trainers`)

* `GET`    `/api/trainers`          – lista (ADMIN)
* `POST`   `/api/trainers`          – tworzenie (ADMIN)
* `GET`    `/api/trainers/{id}`     – pobierz (ADMIN)
* `PUT`    `/api/trainers/{id}`     – aktualizacja (ADMIN)
* `DELETE` `/api/trainers/{id}`     – usuwanie (ADMIN)

### Sesje treningowe (`/api/sessions`)

Role: USER, TRAINER, ADMIN

* `GET`    `/api/sessions`
* `POST`   `/api/sessions`
* `GET`    `/api/sessions/{id}`
* `PUT`    `/api/sessions/{id}`
* `DELETE` `/api/sessions/{id}`

### Rezerwacje (`/api/reservations`)

Role: USER, TRAINER, ADMIN

* `GET`    `/api/reservations`                – wszystkie
* `GET`    `/api/reservations?userId={id}`   – filtrowanie po user
* `GET`    `/api/reservations?sessionId={id}`– filtrowanie po sesji
* `GET`    `/api/reservations/{id}`          – pobierz po ID
* `POST`   `/api/reservations`               – tworzenie
* `DELETE` `/api/reservations/{id}`          – usuwanie

## Zabezpieczenia

Spring Security z HTTP Basic Auth:

* `CustomUserDetailsService` ładuje `User` z repozytorium
* **Role**: USER, TRAINER, ADMIN
* Publiczne: `/swagger-ui/**`, `/v3/api-docs/**`, `/api/auth/**`
* CSRF wyłączone (`.csrf().disable()`)
* Szczegóły w `SecurityConfig`

## Strategy dla zniżek

Pakiet: `org.example.klubfitness.util.strategy`

* **`DiscountStrategy`** – interfejs
* **`noDiscount`** – 0%
* **`seasonalDiscount`** – 15% w grudniu i styczniu
* **`vipDiscount`** – 20% dla ADMIN/TRAINER, 10% dla USER

## Testy i pokrycie

* **Unit tests**: serwisy, strategie, `SecurityConfig` (Surefire)
* **Integration tests**: kontrolery (Failsafe + Testcontainers)
* **Pokrycie** generowane przez JaCoCo

Liczba testów:

* Unit: \~64

![image alt](https://github.com/NKKOW/Klub_Fitness/blob/83d09a1f8e47f1bae4bce46d4bcaa83b4d2b3754/testy_unit.png)
  
* Integration: \~27

![image alt](https://github.com/NKKOW/Klub_Fitness/blob/83d09a1f8e47f1bae4bce46d4bcaa83b4d2b3754/Integration.png)






