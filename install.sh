#!/bin/bash

set -e

echo "=================================================="
echo "Casino Platform - Installation Script"
echo "=================================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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

# Check for required tools
echo "Checking prerequisites..."
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17 or higher."
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi
print_status "Java $JAVA_VERSION detected"

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Apache Maven."
    exit 1
fi
print_status "Maven detected"

# Check Node.js
if ! command -v node &> /dev/null; then
    print_error "Node.js is not installed. Please install Node.js 18 or higher."
    exit 1
fi
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    print_error "Node.js 18 or higher is required. Current version: $NODE_VERSION"
    exit 1
fi
print_status "Node.js v$(node -v) detected"

# Check Python
if ! command -v python3 &> /dev/null; then
    print_error "Python 3 is not installed. Please install Python 3."
    exit 1
fi
print_status "Python $(python3 --version) detected"

# Check PostgreSQL
if ! command -v psql &> /dev/null; then
    print_warning "PostgreSQL client not found. Make sure PostgreSQL server is running."
else
    print_status "PostgreSQL client detected"
fi

echo ""
echo "=================================================="
echo "Installing Backend Dependencies"
echo "=================================================="
echo ""

cd backend
print_status "Building backend with Maven..."
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    print_status "Backend built successfully"
else
    print_error "Backend build failed"
    exit 1
fi
cd ..

echo ""
echo "=================================================="
echo "Installing Frontend User Portal Dependencies"
echo "=================================================="
echo ""

cd frontend-user
print_status "Installing npm packages for user portal..."
npm install
if [ $? -eq 0 ]; then
    print_status "User portal dependencies installed"
else
    print_error "User portal installation failed"
    exit 1
fi
cd ..

echo ""
echo "=================================================="
echo "Installing Frontend Admin Portal Dependencies"
echo "=================================================="
echo ""

cd frontend-admin
print_status "Installing npm packages for admin portal..."
npm install
if [ $? -eq 0 ]; then
    print_status "Admin portal dependencies installed"
else
    print_error "Admin portal installation failed"
    exit 1
fi
cd ..

echo ""
echo "=================================================="
echo "Installation Complete!"
echo "=================================================="
echo ""
echo "Next steps:"
echo "1. Make sure PostgreSQL is running with database 'casino_db'"
echo "2. Update backend/src/main/resources/application.properties with your DB credentials"
echo "3. Run ./start.sh to start all services"
echo ""
print_status "All dependencies installed successfully!"
