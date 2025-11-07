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
        local pid=$(lsof -ti:$port)
        echo -e "${GREEN}✓${NC} $service_name is ${GREEN}RUNNING${NC} on port $port (PID: $pid)"

        # Check if PID file matches actual process
        if [ -f "$pid_file" ]; then
            local file_pid=$(cat "$pid_file")
            if [ "$pid" != "$file_pid" ]; then
                echo -e "  ${YELLOW}⚠${NC} Warning: PID file mismatch (file: $file_pid, actual: $pid)"
            fi
        else
            echo -e "  ${YELLOW}⚠${NC} Warning: PID file not found"
        fi
    else
        echo -e "${RED}✗${NC} $service_name is ${RED}NOT RUNNING${NC} (expected on port $port)"

        # Check if PID file exists for stopped service
        if [ -f "$pid_file" ]; then
            echo -e "  ${YELLOW}⚠${NC} Stale PID file exists: $pid_file"
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
if lsof -i :8080 &> /dev/null && lsof -i :8888 &> /dev/null && lsof -i :3000 &> /dev/null && lsof -i :3001 &> /dev/null; then
    echo -e "${GREEN}✓${NC} All services are running"
else
    echo -e "${YELLOW}⚠${NC} Some services are not running. Run ./start.sh to start them."
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
