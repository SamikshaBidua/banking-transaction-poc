# Banking Transaction POC — Run Guide (Image Distribution)

## Quick Start

```bash
# 1. Extract the zip
unzip banking-poc-images.zip

# 2. Load the Docker images
docker load -i banking-poc-backend.tar
docker load -i banking-poc-frontend.tar

# 3. Start the application
docker compose -f docker-compose.run.yml up

# 4. Open the app
#   Frontend: http://localhost:3000
#   Backend API: http://localhost:8080
```

## Prerequisites

- Docker Engine + Docker Compose
- macOS/Linux: [Colima](https://github.com/abiosoft/colima) or [Rancher Desktop](https://rancherdesktop.io) if Docker Desktop is unavailable
- Windows: Docker Desktop or Rancher Desktop

## Step-by-Step Instructions

### 1. Extract the Zip

```bash
unzip banking-poc-images.zip
```

You should see these files:
- `banking-poc-backend.tar` — Backend Docker image
- `banking-poc-frontend.tar` — Frontend Docker image
- `docker-compose.run.yml` — Compose file to start the app
- `RECIPIENT_SOP.md` — This file

### 2. Load the Images

```bash
docker load -i banking-poc-backend.tar
docker load -i banking-poc-frontend.tar
```

Verify they loaded:
```bash
docker images | grep banking-poc
```

Expected output:
```
banking-poc-backend    v1.0.0    ...
banking-poc-frontend   v1.0.0    ...
```

### 3. Start the Application

```bash
docker compose -f docker-compose.run.yml up
```

To run in the background:
```bash
docker compose -f docker-compose.run.yml up -d
```

### 4. Access the App

| Service | URL |
|---------|-----|
| Frontend (UI) | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| H2 Console | http://localhost:8080/h2-console |

H2 Console credentials:
- JDBC URL: `jdbc:h2:mem:bankingdb`
- Username: `sa`
- Password: *(empty)*

## Demo Credentials

| Role | Username | Password |
|------|----------|----------|
| Super Admin | admin | admin123 |
| Customer | john | john123 |
| Customer | jane | jane123 |

## Sample Cards

| Card Number | PIN | Balance | Holder |
|-------------|-----|---------|--------|
| 4111111111111111 | 1234 | $5,000 | John Doe |
| 4222222222222222 | 5678 | $3,000 | Jane Smith |
| 4333333333333333 | 9999 | $10,000 | John Doe |

Only Visa cards (starting with `4`) are accepted.

## Colima Setup (macOS/Linux without Docker Desktop)

If you use Colima, set the Docker socket path before running any docker commands:

```bash
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
```

If `docker compose` is not found:

```bash
brew install docker-compose
```

Then configure the plugin path:

```bash
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
# Stop containers (keeps images)
docker compose -f docker-compose.run.yml down

# Stop and remove images too
docker compose -f docker-compose.run.yml down --rmi all
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `docker compose` not found | Install via `brew install docker-compose` |
| Cannot connect to Docker daemon | Set `DOCKER_HOST` (see Colima section) |
| Port 8080 already in use | Edit `docker-compose.run.yml`, change `"8080:8080"` to `"8081:8080"` |
| Port 3000 already in use | Edit `docker-compose.run.yml`, change `"3000:80"` to `"3001:80"` |
| Image not found after `docker load` | Verify with `docker images`. Reload if needed. |

## What's Included

These Docker images contain everything needed to run the app — no Java, Maven, Node.js, or npm required on your machine.
