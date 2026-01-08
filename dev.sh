#!/bin/bash

# Development mode launcher for Family Archive
# This script starts both Vite dev server and Spring Boot in parallel

echo "Starting Family Archive in development mode..."
echo ""

# Check if npm dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "Installing npm dependencies..."
    npm install
fi

# Function to cleanup on exit
cleanup() {
    echo ""
    echo "Shutting down..."
    kill 0
}
trap cleanup EXIT

# Start Vite dev server
echo "Starting Vite dev server on port 5173..."
npm run dev &
VITE_PID=$!

# Wait a moment for Vite to start
sleep 2

# Start Spring Boot with dev profile
echo "Starting Spring Boot on port 8080..."
echo ""
echo "====================================="
echo "Application will be available at:"
echo "  http://localhost:8080"
echo "====================================="
echo ""
./gradlew bootRun --args='--spring.profiles.active=dev'

# Wait for all background processes
wait
