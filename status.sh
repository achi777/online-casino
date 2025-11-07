#!/bin/bash

echo "=================================================="
echo "Casino Platform - Service Status"
echo "=================================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get the absolute path of the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Function to check service status
check_service_status() {
    local port=$1
    local service_name=$2
    local pid_file=$3

    # Check if port is in use
    if lsof -i :$port &> /dev/null; then
        # Get all PIDs on this port and take the first one
        local pid=$(lsof -ti:$port | head -1)
        echo -e "${GREEN}✓${NC} $service_name is ${GREEN}RUNNING${NC} on port $port (PID: $pid)"

        # Check if PID file matches actual process
        if [ -f "$pid_file" ]; then
            local file_pid=$(cat "$pid_file")
            if [ "$pid" != "$file_pid" ]; then
                echo -e "  ${YELLOW}⚠${NC} Warning: PID file mismatch (file: $file_pid, actual: $pid)"
                # Update PID file with actual PID
                echo $pid > "$pid_file"
                echo -e "  ${GREEN}✓${NC} PID file updated"
            fi
        else
            echo -e "  ${YELLOW}⚠${NC} Warning: PID file not found, creating it"
            echo $pid > "$pid_file"
        fi
    else
        echo -e "${RED}✗${NC} $service_name is ${RED}NOT RUNNING${NC} (expected on port $port)"

        # Check if PID file exists for stopped service
        if [ -f "$pid_file" ]; then
            echo -e "  ${YELLOW}⚠${NC} Stale PID file exists: $pid_file"
            rm "$pid_file"
            echo -e "  ${GREEN}✓${NC} Removed stale PID file"
        fi
    fi
}

# Check all services
check_service_status 8080 "Backend (Spring Boot)    " "logs/backend.pid"
check_service_status 8888 "Game Server (Python)    " "logs/game-server.pid"
check_service_status 3000 "User Portal (React)     " "logs/frontend-user.pid"
check_service_status 3001 "Admin Portal (React)    " "logs/frontend-admin.pid"

echo ""
echo "=================================================="
echo "Service URLs:"
echo "=================================================="
echo ""

if lsof -i :3000 &> /dev/null; then
    echo -e "${GREEN}User Portal:${NC}  http://localhost:3000"
else
    echo -e "${RED}User Portal:${NC}  Not running"
fi

if lsof -i :3001 &> /dev/null; then
    echo -e "${GREEN}Admin Portal:${NC} http://localhost:3001"
else
    echo -e "${RED}Admin Portal:${NC} Not running"
fi

if lsof -i :8080 &> /dev/null; then
    echo -e "${GREEN}Backend API:${NC}  http://localhost:8080"
else
    echo -e "${RED}Backend API:${NC}  Not running"
fi

if lsof -i :8888 &> /dev/null; then
    echo -e "${GREEN}Game Server:${NC}  http://localhost:8888"
else
    echo -e "${RED}Game Server:${NC}  Not running"
fi

echo ""

# Check if all services are running
BACKEND_UP=$(lsof -i :8080 &> /dev/null && echo 1 || echo 0)
GAMES_UP=$(lsof -i :8888 &> /dev/null && echo 1 || echo 0)
USER_UP=$(lsof -i :3000 &> /dev/null && echo 1 || echo 0)
ADMIN_UP=$(lsof -i :3001 &> /dev/null && echo 1 || echo 0)

TOTAL_UP=$((BACKEND_UP + GAMES_UP + USER_UP + ADMIN_UP))

if [ $TOTAL_UP -eq 4 ]; then
    echo -e "${GREEN}✓${NC} All services are running (4/4)"
else
    echo -e "${YELLOW}⚠${NC} $TOTAL_UP/4 services are running. Run ./start.sh to start missing services."
fi

echo ""

# Suggest actions based on status
if [ $TOTAL_UP -eq 0 ]; then
    echo "To start all services, run: ./start.sh"
elif [ $TOTAL_UP -lt 4 ]; then
    echo "To restart all services, run: ./restart.sh"
fi

echo ""

# Show log file locations
if [ -d "logs" ]; then
    echo "=================================================="
    echo "Log Files:"
    echo "=================================================="
    echo ""
    for log in logs/*.log; do
        if [ -f "$log" ]; then
            size=$(du -h "$log" | cut -f1)
            echo "  $(basename $log) - $size"
        fi
    done
    echo ""
fi
