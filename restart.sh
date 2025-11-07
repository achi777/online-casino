#!/bin/bash

echo "=================================================="
echo "Casino Platform - Restarting All Services"
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

# Stop all services
echo "Step 1: Stopping all services..."
echo ""
if ./stop.sh; then
    print_status "All services stopped"
else
    print_warning "Some issues during shutdown"
fi

# Wait to ensure all processes are fully stopped
echo ""
print_info "Waiting 5 seconds for complete shutdown..."
sleep 5

# Verify all ports are free
echo ""
print_info "Verifying ports are available..."
PORTS_IN_USE=0
for port in 8080 8888 3000 3001; do
    if lsof -i :$port > /dev/null 2>&1; then
        print_warning "Port $port is still in use"
        PORTS_IN_USE=1
    fi
done

if [ $PORTS_IN_USE -eq 1 ]; then
    print_warning "Some ports are still in use. Waiting 5 more seconds..."
    sleep 5
fi

# Start all services
echo ""
echo "Step 2: Starting all services..."
echo ""
if ./start.sh; then
    echo ""
    print_status "Platform restarted successfully!"
    echo ""
    echo "Service URLs:"
    echo "  User Portal:  http://localhost:3000"
    echo "  Admin Portal: http://localhost:3001"
    echo "  Backend API:  http://localhost:8080"
    echo "  Game Server:  http://localhost:8888"
    echo ""
else
    print_error "Failed to start services. Check logs for details."
    exit 1
fi
