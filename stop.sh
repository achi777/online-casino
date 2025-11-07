#!/bin/bash

echo "=================================================="
echo "Casino Platform - Stopping All Services"
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

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Function to stop service by PID file
stop_service() {
    local pid_file=$1
    local service_name=$2

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 2

            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                print_warning "$service_name didn't stop gracefully, force killing..."
                kill -9 $pid
                sleep 1
            fi

            if ps -p $pid > /dev/null 2>&1; then
                print_error "Failed to stop $service_name"
            else
                print_status "$service_name stopped"
                rm "$pid_file"
            fi
        else
            print_warning "$service_name PID file exists but process not found"
            rm "$pid_file"
        fi
    else
        print_warning "$service_name PID file not found"
    fi
}

# Function to stop service by port
stop_by_port() {
    local port=$1
    local service_name=$2

    echo "Checking for $service_name on port $port..."
    local pid=$(lsof -ti:$port)

    if [ ! -z "$pid" ]; then
        echo "Stopping $service_name (PID: $pid)..."
        kill $pid 2>/dev/null
        sleep 2

        # Force kill if still running
        if ps -p $pid > /dev/null 2>&1; then
            print_warning "$service_name didn't stop gracefully, force killing..."
            kill -9 $pid 2>/dev/null
            sleep 1
        fi

        if ps -p $pid > /dev/null 2>&1; then
            print_error "Failed to stop $service_name"
        else
            print_status "$service_name stopped"
        fi
    else
        print_warning "$service_name not running on port $port"
    fi
}

# Stop services using PID files
if [ -d "logs" ]; then
    stop_service "logs/backend.pid" "Backend"
    stop_service "logs/game-server.pid" "Game Server"
    stop_service "logs/frontend-user.pid" "User Portal"
    stop_service "logs/frontend-admin.pid" "Admin Portal"
fi

# Fallback: stop by port if PID files don't work
echo ""
echo "Checking for any remaining processes..."
stop_by_port 8080 "Backend"
stop_by_port 8888 "Game Server"
stop_by_port 3000 "User Portal"
stop_by_port 3001 "Admin Portal"

# Stop any remaining npm/vite processes
echo ""
echo "Checking for npm/vite processes..."
pkill -f "vite.*frontend-user" 2>/dev/null && print_status "Stopped user portal vite process" || true
pkill -f "vite.*frontend-admin" 2>/dev/null && print_status "Stopped admin portal vite process" || true

# Stop any remaining Java processes with casino-platform
echo "Checking for casino backend processes..."
pkill -f "casino-platform-1.0.0.jar" 2>/dev/null && print_status "Stopped backend process" || true

# Stop any remaining Python game server
echo "Checking for game server processes..."
pkill -f "http.server 8888" 2>/dev/null && print_status "Stopped game server process" || true

echo ""
echo "=================================================="
echo "All Services Stopped"
echo "=================================================="
echo ""

# Clean up log files if requested
if [ "$1" == "--clean" ]; then
    echo "Cleaning up log files..."
    rm -rf logs/*.log
    print_status "Log files cleaned"
    echo ""
fi

print_status "Platform stopped successfully"
echo ""
echo "Available commands:"
echo "  ./start.sh   - Start all services"
echo "  ./restart.sh - Restart all services"
echo "  ./status.sh  - Check service status"
