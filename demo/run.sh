#!/bin/bash
set -e

# Run Gradle build from project root
echo "Running Gradle build..."
./gradlew clean build

# Navigate to demo directory run docker compose
echo "Building and starting Docker services..."
cd demo
docker compose up
