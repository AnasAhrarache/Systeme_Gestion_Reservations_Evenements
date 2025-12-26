# EventPro

> A Vaadin + Spring Boot event management web application (UI + backend) that lists and manages events with filtering, sample data seeding, and a responsive frontend.

---

## Summary

EventPro is a Java web application combining a Spring Boot backend and a Vaadin-based frontend. It provides public event listing pages (filters, search, cards) and server-side services to load events (sample data is provided in `src/main/resources/data.sql`). The UI components are implemented in Java (Vaadin Flow), examples include `EventListView` which displays and filters events.

## Key features

- Event listing with search and multiple filters (category, city, date range, price)
- Responsive card-based UI (Vaadin components)
- Sample SQL seed data (`data.sql`) for quick local demos
- Spring Boot backend with service layer for querying events

## Prerequisites

- Java 11 or newer (Java 17 recommended)
- Maven 3.6+ (the project includes the Maven wrapper so installing Maven is optional)
- (Optional) Node/npm only if you plan advanced frontend customization — the Maven build handles Vaadin frontend build by default

## Build & Run

Use the included Maven wrapper for consistent behavior across platforms.

Windows (PowerShell / CMD):

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Unix / macOS:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

After running, open your browser at:

- http://localhost:8080/events — Public event list (the `EventListView` route)

To run the produced jar:

```bash
java -jar target/*.jar
```

## Tests

Run unit and integration tests with:

```bash
./mvnw test
```

## Frontend / Vaadin notes

- The project uses Vaadin Flow (Java-based UI). The `frontend/` folder contains generated assets and static files used by Vaadin.
- The Maven build triggers Vaadin's frontend build; you normally don't need to run npm manually.
- For heavy frontend development (npm toolchain), see `frontend/` for resources and generated output.

## Configuration

- Application properties are in `src/main/resources/application.properties`. Edit database connection, ports and other Spring properties there.
- Sample data is in `src/main/resources/data.sql`. The application will use the configured datasource; by default many projects use an embedded H2 for dev — check `application.properties` to confirm.

## Project structure (high level)

- `pom.xml` - Maven build file
- `mvnw`, `mvnw.cmd` - Maven wrapper for cross-platform builds
- `src/main/java` - Application Java source (controllers, services, views)
  - `com.event.views` - Vaadin UI views (examples: `EventListView`)
  - `com.event.service` - Service layer for business logic (e.g., `EventService`)
- `src/main/resources` - Configuration and SQL seed data (`application.properties`, `data.sql`)
- `frontend/` - Vaadin frontend and generated assets

## Important files / entry points

- Public event list UI: [src/main/java/com/event/views/publics/EventListView.java](src/main/java/com/event/views/publics/EventListView.java#L1)
- Application config: [src/main/resources/application.properties](src/main/resources/application.properties#L1)
- Sample data: [src/main/resources/data.sql](src/main/resources/data.sql#L1)

## Troubleshooting

- If the app fails to start due to database connectivity, update datasource settings in `application.properties` to point to a valid DB or use an embedded DB.
- If frontend assets are missing, run a full Maven clean build to regenerate Vaadin frontend resources:

```bash
./mvnw clean package -DskipTests=false
```

## Development tips

- Use the Maven wrapper (`mvnw` / `mvnw.cmd`) to avoid local Maven version issues.
- Modify UI components under `com.event.views` — Vaadin views are regular Java classes.
- Services and repositories follow standard Spring Boot patterns. Search for `EventService` and repository interfaces to extend or adapt queries.

## Next steps / ideas

- Add README sections for contributing, code style, or API docs if this becomes a shared project.
- Provide explicit instructions for setting production-ready DB (e.g., PostgreSQL) and environment variables for credentials.

---

If you'd like, I can:

- add a quickstart section tailored to your current `application.properties` contents; or
- run the build here and report any build errors.

File created: [README.md](README.md)
