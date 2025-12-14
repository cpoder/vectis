#!/bin/bash
# Start Vectis Server (standalone mode)

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Starting Vectis Server ==="

cd "$PROJECT_DIR/vectis-server"

# Check if JAR exists
JAR_FILE=$(ls target/vectis-server-*.jar 2>/dev/null | head -1)
if [ -z "$JAR_FILE" ]; then
  echo "Building vectis-server..."
  cd "$PROJECT_DIR/vectis-pesit" && mvn install -DskipTests -q
  cd "$PROJECT_DIR/vectis-server" && mvn package -DskipTests -q
  JAR_FILE=$(ls target/vectis-server-*.jar 2>/dev/null | head -1)
fi

if [ -z "$JAR_FILE" ]; then
  echo "ERROR: Could not find or build vectis-server JAR"
  exit 1
fi

echo "Starting $JAR_FILE..."
java -jar "$JAR_FILE"
