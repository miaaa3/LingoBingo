#!/bin/bash
# LingoBingo — one-click start.
# macOS: double-click this file in Finder.  Linux: ./start.command
# Needs Docker Desktop running. First start takes a few minutes (it builds everything).
set -e
cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed. Get Docker Desktop: https://www.docker.com/products/docker-desktop/"
  read -r -p "Press Enter to close..." _; exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "Docker is installed but not running. Start Docker Desktop, then run this again."
  read -r -p "Press Enter to close..." _; exit 1
fi

# First run: create .env with fresh random secrets (never committed).
if [ ! -f .env ]; then
  rand() { LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c "$1"; }
  cat > .env <<EOF
SECRET_KEY=$(rand 64)
MYSQL_ROOT_PASSWORD=$(rand 24)
FRONTEND_PORT=8088
EOF
  echo "Created .env with random secrets."
fi

PORT=$(grep -E '^FRONTEND_PORT=' .env | cut -d= -f2); PORT=${PORT:-8088}
URL="http://localhost:$PORT"

echo "Building and starting LingoBingo (database, backend, frontend)..."
docker compose up --build -d

echo "Waiting for the app to answer on $URL ..."
for _ in $(seq 1 120); do
  # nginx answers 502/503/504 until the backend is up; any other code means ready.
  CODE=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$URL/api/authentication/login" \
         -H 'Content-Type: application/json' -d '{}' 2>/dev/null || true)
  case "$CODE" in 000|502|503|504) ;; *) break ;; esac
  sleep 3
done

echo ""
echo "LingoBingo is running at $URL"
echo "Demo teacher: teacher@lingobingo.com / password123"
echo "Stop it later with:  docker compose down"
( command -v open >/dev/null && open "$URL" ) || ( command -v xdg-open >/dev/null && xdg-open "$URL" ) || true
