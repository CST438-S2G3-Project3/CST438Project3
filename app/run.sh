#!/usr/bin/env bash

set -e

# Get the directory containing this script (/app)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Root .env is one directory above /app
ENV_FILE="$SCRIPT_DIR/../.env"

# Make sure the root .env exists
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found at:"
    echo "$ENV_FILE"
    echo ""
    echo "Create one from .env.example first."
    exit 1
fi

echo "Loading environment variables from root .env..."

# Export all variables from .env
set -a
source "$ENV_FILE"
set +a

# Make sure required frontend variables exist
if [ -z "${EXPO_PUBLIC_SUPABASE_URL:-}" ]; then
    echo "Error: EXPO_PUBLIC_SUPABASE_URL is not set in .env"
    exit 1
fi

if [ -z "${EXPO_PUBLIC_SUPABASE_PUBLISHABLE_KEY:-}" ]; then
    echo "Error: EXPO_PUBLIC_SUPABASE_PUBLISHABLE_KEY is not set in .env"
    exit 1
fi

echo "Environment loaded."
echo "Starting Expo..."

# Always run Expo from the app directory
cd "$SCRIPT_DIR"

exec npx expo start "$@"