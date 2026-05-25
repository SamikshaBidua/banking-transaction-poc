# Banking Transaction POC — Run Guide

## Quick Start

```bash
# 1. Extract the zip
cd banking-transaction-poc

# 2. Start the application
docker compose up

# 3. Open the app
#   Frontend: http://localhost:3000
#   Backend API: http://localhost:8080
```

## Prerequisites

- Docker Engine + Docker Compose
- macOS/Linux: [Colima](https://github.com/abiosoft/colima) or [Rancher Desktop](https://rancherdesktop.io) if Docker Desktop is unavailable
- Windows: Docker Desktop or Rancher Desktop

## Demo Credentials

| Role        | Username | Password  |
|-------------|----------|-----------|
| Super Admin | admin    | admin123  |
| Customer    | john     | john123   |
| Customer    | jane     | jane123   |

## Sample Cards

| Card Number      | PIN  | Balance  | Holder    |
|------------------|------|----------|-----------|
| 4111111111111111 | 1234 | $5,000   | John Doe  |
| 4222222222222222 | 5678 | $3,000   | Jane Smith|
| 4333333333333333 | 9999 | $10,000  | John Doe  |

> Only Visa cards (starting with `4`) are accepted.

## Colima-Specific Setup

If you use Colima and see `docker compose` errors:

```bash
# Set the Docker socket path
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"

# Make it permanent
echo 'export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"' >> ~/.zshrc
```

If `docker compose` is not found:

```bash
brew install docker-compose
mkdir -p ~/.docker
cat > ~/.docker/config.json << 'EOF'
{
  "cliPluginsExtraDirs": [
    "/opt/homebrew/lib/docker/cli-plugins"
  ]
}
EOF
```

## Rancher Desktop Setup

1. Open Rancher Desktop → Preferences → Container Engine
2. Select **dockerd (moby)**
3. Apply & Restart
4. Verify: `docker compose version`

## Stopping the App

```bash
# Stop (keeps data)
docker compose down

# Stop and remove everything
docker compose down --volumes --rmi all
```

## Troubleshooting

| Problem                                | Solution                                           |
|----------------------------------------|----------------------------------------------------|
| `docker compose` not found             | Install via `brew install docker-compose`           |
| Socket not found                       | Set `DOCKER_HOST` (see Colima section above)       |
| Port 8080 already in use               | Change port in `docker-compose.yml`: `"8081:8080"` |
| Port 3000 already in use               | Change port in `docker-compose.yml`: `"3001:80"`   |
| "no matching manifest for linux/arm64" | Images are pre-built for ARM64; should not occur   |

## What's Inside

| Component | Description                        |
|-----------|------------------------------------|
| `backend/` | Pre-built Spring Boot JAR (port 8080) |
| `frontend/` | Pre-built React app served via nginx (port 3000) |
| `docker-compose.yml` | Orchestrates both services         |
