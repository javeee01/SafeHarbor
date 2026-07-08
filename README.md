# SafeHarbor

Disaster Response and Emergency Coordination Platform.

## Structure

```
SafeHarbor/
├── frontend/   → Static HTML/CSS/JS (deployed on GitHub Pages)
└── backend/    → Spring Boot REST API (Java 17 + MySQL)
```

## Frontend

Served as static files via GitHub Pages.

**Live URL**: https://javeee01.github.io/SafeHarbor/

## Backend

Spring Boot 3.5 REST API with JWT authentication.

### Run locally
```bash
cd backend
./mvnw spring-boot:run
```

### Requirements
- Java 17+
- MySQL 8+ (database: `safeharbor_db`)

## API Endpoints

| Module     | Base Path          |
|------------|--------------------|
| Auth       | `/api/auth`        |
| Incidents  | `/api/incidents`   |
| Inventory  | `/api/inventory`   |
| Shelters   | `/api/shelters`    |
| Dispatch   | `/api/dispatches`  |
