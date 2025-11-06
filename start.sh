#!/bin/bash

set -e

echo "=================================================="
echo "Casino Platform - Starting All Services"
echo "=================================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the absolute path of the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Create logs directory if it doesn't exist
mkdir -p logs

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

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Function to check if port is in use
check_port() {
    lsof -i :$1 &> /dev/null
    return $?
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=0

    print_info "Waiting for $service_name to be ready..."

    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            print_status "$service_name is ready!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done

    print_error "$service_name failed to start within expected time"
    return 1
}

echo "Checking for running services..."
echo ""

# Check if services are already running
if check_port 8080; then
    print_warning "Backend already running on port 8080"
else
    print_info "Starting Backend (Spring Boot)..."
    cd "$SCRIPT_DIR/backend"
    java -jar target/casino-platform-1.0.0.jar > "$SCRIPT_DIR/logs/backend.log" 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > "$SCRIPT_DIR/logs/backend.pid"
    print_status "Backend started (PID: $BACKEND_PID)"
    cd "$SCRIPT_DIR"
fi

if check_port 8888; then
    print_warning "Game server already running on port 8888"
else
    print_info "Starting Game Server (Python HTTP)..."
    python3 -m http.server 8888 --directory games > "$SCRIPT_DIR/logs/game-server.log" 2>&1 &
    GAME_SERVER_PID=$!
    echo $GAME_SERVER_PID > "$SCRIPT_DIR/logs/game-server.pid"
    print_status "Game server started (PID: $GAME_SERVER_PID)"
fi

if check_port 3000; then
    print_warning "User portal already running on port 3000"
else
    print_info "Starting User Portal (React)..."
    cd "$SCRIPT_DIR/frontend-user"
    npm run dev > "$SCRIPT_DIR/logs/frontend-user.log" 2>&1 &
    USER_PORTAL_PID=$!
    echo $USER_PORTAL_PID > "$SCRIPT_DIR/logs/frontend-user.pid"
    print_status "User portal started (PID: $USER_PORTAL_PID)"
    cd "$SCRIPT_DIR"
fi

if check_port 3001; then
    print_warning "Admin portal already running on port 3001"
else
    print_info "Starting Admin Portal (React)..."
    cd "$SCRIPT_DIR/frontend-admin"
    npm run dev > "$SCRIPT_DIR/logs/frontend-admin.log" 2>&1 &
    ADMIN_PORTAL_PID=$!
    echo $ADMIN_PORTAL_PID > "$SCRIPT_DIR/logs/frontend-admin.pid"
    print_status "Admin portal started (PID: $ADMIN_PORTAL_PID)"
    cd "$SCRIPT_DIR"
fi

echo ""
echo "=================================================="
echo "Waiting for services to be ready..."
echo "=================================================="
echo ""

sleep 5

# Wait for backend to be ready
wait_for_service "http://localhost:8080/api/user/games" "Backend API"

echo ""
echo "=================================================="
echo "All Services Started Successfully!"
echo "=================================================="
echo ""
echo "Service URLs:"
echo "  ${GREEN}User Portal:${NC}  http://localhost:3000"
echo "  ${GREEN}Admin Portal:${NC} http://localhost:3001"
echo "  ${GREEN}Backend API:${NC}  http://localhost:8080"
echo "  ${GREEN}Game Server:${NC}  http://localhost:8888"
echo ""
echo "Default Credentials:"
echo "  ${YELLOW}User:${NC}  test@casino.ge / Test1234"
echo "  ${YELLOW}Admin:${NC} owner@casino.ge / Test1234"
echo ""
echo "Logs are available in: $SCRIPT_DIR/logs/"
echo "To stop all services, run: ./stop.sh"
echo ""
print_status "Platform is ready for use!"
