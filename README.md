# Event Management System (Vaadin + Spring Boot)

Vaadin Flow single-page application for managing events and reservations with distinct administrator, organizer, and client experiences. The backend is built with Spring Boot 3 and persists data in an H2 database seeded from `data.sql`.

## Features
- Public catalogue with event cards, detail pages, and reservation entry guarded by login/registration
- Client area: dashboard for upcoming events, browse/reserve seats, view history, cancel within allowed windows, and manage profile
- Organizer area: create/edit/publish events, monitor capacity and availability, manage reservations (confirm/cancel), and view organizer dashboards
- Admin area: manage users, oversee all events and reservations, and view global statistics
- Authentication: email/password login, registration with validation, session-based navigation per role; seeded plain-text passwords migrate to bcrypt on first login
- UX: responsive Vaadin layout with role-aware side navigation and a light/dark theme toggle backed by `static/themes/dark-mode.css`

## Tech Stack
- Java 17, Spring Boot 3.2, Vaadin 24
- Spring Data JPA, H2 database (file-based)
- Maven Wrapper (`mvnw`, `mvnw.cmd`), optional Lombok

## Project Layout
- `src/main/java/com/event`
  - `model/entities`: domain models (`Event`, `Reservation`, `User`) with validation and lifecycle hooks
  - `model/enums`: event categories/statuses, reservation status, and user roles
  - `repository`: Spring Data repositories with custom queries for filtering and stats
  - `service`: business services for events, reservations, users, and theme management (publishing, capacity checks, reservation limits, password rules)
  - `views`: Vaadin routes for public (`publics`), client, organizer, and admin areas plus `MainLayout` navigation
  - `security`: session storage and role-based navigation helpers
  - `util`: date validation, password encoding (bcrypt), and reservation code generation
- `src/main/resources/application.properties`: H2 connection, JPA init (`create-drop`), Vaadin dev settings
- `src/main/resources/data.sql`: demo seed for users, events, and reservations
- `src/main/resources/static/themes/dark-mode.css`: custom dark theme overrides

## Running Locally
1) Prerequisites: Java 17; Maven is optional because the wrapper is included. Vaadin will download frontend tooling automatically; having `pnpm` available speeds up the first run.
2) Install dependencies and start the app:
   - Windows: `mvnw.cmd spring-boot:run`
   - Unix/macOS: `./mvnw spring-boot:run`
3) Open http://localhost:8080
4) H2 console (if needed): http://localhost:8080/h2-console  
   - JDBC URL: `jdbc:h2:file:C:/Users/PC/test`  
   - Username: `sa` (no password by default)

The database is recreated on start (`spring.jpa.hibernate.ddl-auto=create-drop`) and seeded from `src/main/resources/data.sql`.

## Demo Accounts
Use these credentials to explore each role:
- Admin: `admin@event.ma` / `admin123`
- Organizer: `organizer1@event.ma` / `org123`
- Client: `client1@event.ma` / `client123`

## Testing
- Run the test suite: `mvnw.cmd test` (Windows) or `./mvnw test` (Unix/macOS).

## Notes
- To persist data across restarts, switch `spring.jpa.hibernate.ddl-auto` to `update` and point `spring.datasource.url` to your preferred path.
- The theme toggle stores the choice in `localStorage` (`app_theme`) and falls back to light mode.
