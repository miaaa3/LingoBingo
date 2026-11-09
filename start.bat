@echo off
rem LingoBingo - one-click start for Windows. Double-click this file.
rem Needs Docker Desktop running. First start takes a few minutes.
cd /d "%~dp0"

where docker >nul 2>nul || (echo Docker is not installed. Get Docker Desktop: https://www.docker.com/products/docker-desktop/ & pause & exit /b 1)
docker info >nul 2>nul || (echo Docker is installed but not running. Start Docker Desktop, then run this again. & pause & exit /b 1)

if not exist .env (
  powershell -NoProfile -Command "$r={param($n) -join ((48..57)+(65..90)+(97..122) | Get-Random -Count $n | ForEach-Object {[char]$_})}; \"SECRET_KEY=$(& $r 62)`nMYSQL_ROOT_PASSWORD=$(& $r 24)`nFRONTEND_PORT=8088\" | Set-Content -Encoding ascii .env"
  echo Created .env with random secrets.
)

echo Building and starting LingoBingo (database, backend, frontend)...
docker compose up --build -d || (pause & exit /b 1)

echo Waiting for the app to start...
timeout /t 45 /nobreak >nul
echo.
echo LingoBingo is running at http://localhost:8088
echo Demo teacher: teacher@lingobingo.com / password123
echo Stop it later with:  docker compose down
start "" http://localhost:8088
pause
