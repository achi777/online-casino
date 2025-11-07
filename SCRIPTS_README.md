# Casino Platform - Management Scripts

This directory contains scripts to manage the entire Casino Platform, including backend, frontend-user, frontend-admin, and game server.

## Prerequisites

Before running these scripts, ensure you have the following installed:

- **Java 17+** - Backend (Spring Boot)
- **Maven** - Backend build tool
- **Node.js 18+** - Frontend (React/Vite)
- **Python 3** - Game server (HTTP server)
- **PostgreSQL** - Database

## Scripts Overview

### 1. install.sh
**Purpose**: Install all dependencies and build the platform

**Usage**:
```bash
./install.sh
```

**What it does**:
- Checks for required tools (Java, Maven, Node.js, Python, PostgreSQL)
- Builds backend with Maven (`mvn clean install -DskipTests`)
- Installs frontend-user dependencies (`npm install`)
- Installs frontend-admin dependencies (`npm install`)
- Verifies games directory exists
- Creates logs directory
- Saves installation logs to `logs/install-*.log`

**When to run**:
- First time setup
- After pulling new code with dependency changes
- After cleaning build artifacts

---

### 2. start.sh
**Purpose**: Start all platform services

**Usage**:
```bash
./start.sh
```

**What it does**:
- Creates logs directory if it doesn't exist
- Checks if services are already running
- Starts Backend (Spring Boot) on port 8080
- Starts Game Server (Python HTTP) on port 8888
- Starts User Portal (React/Vite) on port 3000
- Starts Admin Portal (React/Vite) on port 3001
- Verifies each service started successfully
- Waits for backend API to be ready
- Saves PIDs to `logs/*.pid` files
- Saves service logs to `logs/*.log` files

**Service URLs after startup**:
- User Portal: http://localhost:3000
- Admin Portal: http://localhost:3001
- Backend API: http://localhost:8080
- Game Server: http://localhost:8888

**Default Credentials**:
- User: test@casino.ge / Test1234
- Admin: owner@casino.ge / Test1234

---

### 3. stop.sh
**Purpose**: Stop all platform services

**Usage**:
```bash
./stop.sh
./stop.sh --clean  # Also clean log files
```

**What it does**:
- Stops services using PID files
- Falls back to stopping by port if PID files are missing
- Kills remaining npm/vite/Java/Python processes
- Gracefully terminates with SIGTERM, forces with SIGKILL if needed
- Optionally cleans log files with `--clean` flag

---

### 4. restart.sh
**Purpose**: Restart all platform services

**Usage**:
```bash
./restart.sh
```

**What it does**:
- Stops all services using `./stop.sh`
- Waits 5 seconds for complete shutdown
- Verifies all ports are free (8080, 8888, 3000, 3001)
- Starts all services using `./start.sh`
- Displays service URLs

**When to use**:
- After code changes
- After configuration changes
- To resolve service issues

---

### 5. status.sh
**Purpose**: Check status of all services

**Usage**:
```bash
./status.sh
```

**What it does**:
- Checks if each service is running
- Displays PID for each running service
- Auto-fixes PID file mismatches
- Removes stale PID files
- Shows service URLs
- Displays log file sizes
- Suggests actions based on current status

**Output includes**:
- Backend status (port 8080)
- Game Server status (port 8888)
- User Portal status (port 3000)
- Admin Portal status (port 3001)
- Running count (e.g., "3/4 services are running")

---

## Common Workflows

### Initial Setup
```bash
# 1. Install dependencies
./install.sh

# 2. Configure database (update backend/src/main/resources/application.properties)

# 3. Start all services
./start.sh

# 4. Check status
./status.sh
```

### Daily Development
```bash
# Start services
./start.sh

# Check status
./status.sh

# Stop services when done
./stop.sh
```

### After Code Changes
```bash
# Rebuild and restart
./install.sh
./restart.sh
```

### Troubleshooting
```bash
# Check service status
./status.sh

# View logs
tail -f logs/backend.log
tail -f logs/frontend-user.log
tail -f logs/frontend-admin.log
tail -f logs/game-server.log

# Restart services
./restart.sh

# Clean restart (stop, clean logs, start)
./stop.sh --clean
./start.sh
```

---

## Directory Structure

```
gambling/
├── backend/                  # Spring Boot backend
├── frontend-user/           # React user portal
├── frontend-admin/          # React admin portal
├── games/                   # Game files (served by Python HTTP)
├── logs/                    # Service logs and PID files
│   ├── backend.log
│   ├── backend.pid
│   ├── game-server.log
│   ├── game-server.pid
│   ├── frontend-user.log
│   ├── frontend-user.pid
│   ├── frontend-admin.log
│   └── frontend-admin.pid
├── install.sh              # Install dependencies
├── start.sh                # Start all services
├── stop.sh                 # Stop all services
├── restart.sh              # Restart all services
└── status.sh               # Check service status
```

---

## Services Details

### Backend (Spring Boot)
- **Port**: 8080
- **Type**: Java JAR
- **Startup**: `java -jar backend/target/casino-platform-1.0.0.jar`
- **Health Check**: `http://localhost:8080/api/user/games`
- **Log**: `logs/backend.log`

### Game Server (Python HTTP)
- **Port**: 8888
- **Type**: Python HTTP Server
- **Startup**: `python3 -m http.server 8888 --directory games`
- **Serves**: Static HTML/JS/CSS game files
- **Log**: `logs/game-server.log`

### User Portal (React/Vite)
- **Port**: 3000
- **Type**: Node.js/Vite dev server
- **Startup**: `npm run dev` in frontend-user/
- **Public Access**: http://localhost:3000
- **Log**: `logs/frontend-user.log`

### Admin Portal (React/Vite)
- **Port**: 3001
- **Type**: Node.js/Vite dev server
- **Startup**: `npm run dev` in frontend-admin/
- **Admin Access**: http://localhost:3001
- **Log**: `logs/frontend-admin.log`

---

## Error Handling

All scripts include:
- ✅ Prerequisite checks
- ✅ Directory existence verification
- ✅ Process startup verification
- ✅ PID file management
- ✅ Port availability checks
- ✅ Graceful error messages
- ✅ Exit codes (0 = success, 1 = failure)

---

## Notes

- Scripts use `nohup` to ensure processes continue after terminal closes
- PID files are automatically created and managed
- Stale PID files are automatically cleaned up by `status.sh`
- All scripts are safe to run multiple times
- Services already running will not be restarted by `start.sh`
- Use `restart.sh` to force restart all services

---

## Support

For issues or questions:
1. Check logs in `logs/` directory
2. Run `./status.sh` to verify service status
3. Try `./restart.sh` to resolve issues
4. Check database connection in application.properties
