#!/usr/bin/env bash

set -e

# Get the directory containing this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# The .env file is one directory above /api
ENV_FILE="$SCRIPT_DIR/../.env"

# Make sure .env exists
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found at:"
    echo "$ENV_FILE"
    echo ""
    echo "Create one from .env.example first."
    exit 1
fi

echo "Loading environment variables from .env..."

# Export variables loaded from .env
set -a
source "$ENV_FILE"
set +a

# Make sure the database password exists
if [ -z "${SUPABASE_DB_PASSWORD:-}" ]; then
    echo "Error: SUPABASE_DB_PASSWORD is not set in .env"
    exit 1
fi

echo "Environment loaded."
echo "Starting Spring Boot..."

# Always run Gradle from the api directory
cd "$SCRIPT_DIR"

exec ./gradlew bootRun