#!/bin/bash

echo "=================================================="
echo "Casino Platform - Restarting All Services"
echo "=================================================="
echo ""

# Colors for output
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

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Stop all services
echo "Step 1: Stopping all services..."
echo ""
./stop.sh

# Wait a bit to ensure all processes are fully stopped
echo ""
echo "Waiting 3 seconds for complete shutdown..."
sleep 3

# Start all services
echo ""
echo "Step 2: Starting all services..."
echo ""
./start.sh

if [ $? -eq 0 ]; then
    echo ""
    print_status "Platform restarted successfully!"
else
    print_warning "There were issues during restart. Check logs for details."
    exit 1
fi
