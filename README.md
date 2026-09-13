# LingoBingo

Monorepo for LingoBingo: an Angular frontend and a Spring Boot backend for a
quiz, flashcard, and live multiplayer-game learning app.

## Layout
- `frontend/` - Angular 16 app (served by nginx in Docker)
- `backend/`  - Spring Boot 3 / Java 17 REST API
- `docker-compose.yml` - runs frontend + backend + MySQL together

## Run with Docker (recommended)
1. Create your env file and set the two secrets:
   ```
   cp .env.example .env      # set SECRET_KEY and MYSQL_ROOT_PASSWORD
   ```
2. Build and start everything:
   ```
   docker compose up --build
   ```
3. Open http://localhost:8088

The API is reached at `/api` through the nginx proxy (no CORS needed). MySQL
data persists in the `dbdata` volume. Stop with `docker compose down`
(add `-v` to also delete the database volume).

## Run locally without Docker
Backend (needs JDK 17 and a local MySQL):
```
cd backend
cp .env.example .env         # set SECRET_KEY; adjust DB_* for your MySQL
./mvnw spring-boot:run
```
Frontend:
```
cd frontend
npm install
npm start                    # http://localhost:4200, calls the backend on :8081
```

## Notes
- Secrets live in `.env` files, which are gitignored. Never commit real secrets.
- `ddl-auto` is `update`, so the schema evolves without wiping data on restart.
