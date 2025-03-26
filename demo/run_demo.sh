#!/bin/bash
set -e

# Save the current directory
ORIGINAL_DIR=$(pwd)
cd "$(dirname "$(readlink -f "$0")")"

# Navigate to demo directory run docker compose
echo "Building and starting Docker services..."
docker compose up

# Return to the original directory when done
cd "$ORIGINAL_DIR"
