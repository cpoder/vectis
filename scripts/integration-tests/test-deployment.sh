#!/bin/bash
#
# PeSIT Wizard Deployment Test
# Validates Docker/Kubernetes deployment and stability
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Configuration
SERVER_PORT=8080
PESIT_PORT=1761
HEALTH_TIMEOUT=60
STABILITY_DURATION=30

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "[INFO] $1"; }
log_pass() { echo -e "${GREEN}[PASS]${NC} $1"; }
log_fail() { echo -e "${RED}[FAIL]${NC} $1"; return 1; }

ERRORS=0

cleanup() {
    log_info "Cleaning up test deployment..."
    cd "$PROJECT_ROOT/pesitwizard-server"
    docker compose -f docker-compose.yml down 2>/dev/null || true
}

trap cleanup EXIT

echo "============================================"
echo "  Deployment Tests"
echo "============================================"

# Check Docker availability
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}[SKIP]${NC} Docker not available, skipping deployment tests"
    exit 0
fi

# ============================================
# Test 1: Docker Compose Deployment
# ============================================
log_info "Test 1: Starting Docker Compose deployment..."

cd "$PROJECT_ROOT/pesitwizard-server"

# Start services
if docker compose -f docker-compose.yml up -d 2>/dev/null; then
    log_pass "Docker Compose started"
else
    log_fail "Docker Compose failed to start"
    ((ERRORS++))
    exit 1
fi

# ============================================
# Test 2: Wait for Health Check
# ============================================
log_info "Test 2: Waiting for server health (timeout: ${HEALTH_TIMEOUT}s)..."

HEALTH_URL="http://localhost:${SERVER_PORT}/actuator/health"
ELAPSED=0

while [ $ELAPSED -lt $HEALTH_TIMEOUT ]; do
    if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
        log_pass "Server is healthy after ${ELAPSED}s"
        break
    fi
    sleep 2
    ((ELAPSED+=2))
done

if [ $ELAPSED -ge $HEALTH_TIMEOUT ]; then
    log_fail "Server health check timeout"
    ((ERRORS++))
fi

# ============================================
# Test 3: API Accessibility
# ============================================
log_info "Test 3: Checking API accessibility..."

# Check health endpoint
HEALTH_RESPONSE=$(curl -sf "$HEALTH_URL" 2>/dev/null || echo "error")
if [[ "$HEALTH_RESPONSE" == *"UP"* ]]; then
    log_pass "Health endpoint accessible"
else
    log_fail "Health endpoint not accessible"
    ((ERRORS++))
fi

# Check info endpoint
INFO_URL="http://localhost:${SERVER_PORT}/actuator/info"
if curl -sf "$INFO_URL" > /dev/null 2>&1; then
    log_pass "Info endpoint accessible"
else
    log_fail "Info endpoint not accessible"
    ((ERRORS++))
fi

# Check server status endpoint
STATUS_URL="http://localhost:${SERVER_PORT}/api/v1/server/status"
if curl -sf "$STATUS_URL" > /dev/null 2>&1; then
    log_pass "Server status endpoint accessible"
else
    echo -e "${YELLOW}[WARN]${NC} Server status endpoint not accessible (may require auth)"
fi

# ============================================
# Test 4: PeSIT Port Accessibility
# ============================================
log_info "Test 4: Checking PeSIT port (${PESIT_PORT})..."

if nc -z localhost $PESIT_PORT 2>/dev/null; then
    log_pass "PeSIT port $PESIT_PORT is listening"
else
    log_fail "PeSIT port $PESIT_PORT is not listening"
    ((ERRORS++))
fi

# ============================================
# Test 5: Stability Test
# ============================================
log_info "Test 5: Running stability test (${STABILITY_DURATION}s)..."

STABILITY_ERRORS=0
START_TIME=$(date +%s)

while [ $(($(date +%s) - START_TIME)) -lt $STABILITY_DURATION ]; do
    # Check health every 5 seconds
    if ! curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
        ((STABILITY_ERRORS++))
        log_info "Health check failed at $(date)"
    fi
    
    # Check container status
    if ! docker compose -f docker-compose.yml ps 2>/dev/null | grep -q "running"; then
        ((STABILITY_ERRORS++))
        log_info "Container not running at $(date)"
    fi
    
    sleep 5
done

if [ $STABILITY_ERRORS -eq 0 ]; then
    log_pass "Server remained stable for ${STABILITY_DURATION}s"
else
    log_fail "Server had $STABILITY_ERRORS stability issues"
    ((ERRORS++))
fi

# ============================================
# Test 6: Memory and Resource Check
# ============================================
log_info "Test 6: Checking resource usage..."

CONTAINER_ID=$(docker compose -f docker-compose.yml ps -q pesitwizard-server 2>/dev/null | head -1)

if [ -n "$CONTAINER_ID" ]; then
    STATS=$(docker stats --no-stream --format "{{.MemUsage}} | {{.CPUPerc}}" "$CONTAINER_ID" 2>/dev/null)
    log_info "Container stats: $STATS"
    log_pass "Resource check completed"
else
    echo -e "${YELLOW}[WARN]${NC} Could not get container stats"
fi

# ============================================
# Test 7: Logs Check
# ============================================
log_info "Test 7: Checking logs for errors..."

ERROR_COUNT=$(docker compose -f docker-compose.yml logs 2>/dev/null | grep -ci "error\|exception\|failed" || echo "0")

if [ "$ERROR_COUNT" -lt 5 ]; then
    log_pass "Logs check passed ($ERROR_COUNT potential issues)"
else
    echo -e "${YELLOW}[WARN]${NC} Found $ERROR_COUNT potential errors in logs"
fi

# ============================================
# Test 8: Graceful Shutdown
# ============================================
log_info "Test 8: Testing graceful shutdown..."

SHUTDOWN_START=$(date +%s)
docker compose -f docker-compose.yml stop 2>/dev/null
SHUTDOWN_END=$(date +%s)
SHUTDOWN_TIME=$((SHUTDOWN_END - SHUTDOWN_START))

if [ $SHUTDOWN_TIME -lt 30 ]; then
    log_pass "Graceful shutdown completed in ${SHUTDOWN_TIME}s"
else
    log_fail "Shutdown took too long: ${SHUTDOWN_TIME}s"
    ((ERRORS++))
fi

# ============================================
# Summary
# ============================================
echo ""
echo "============================================"
if [ $ERRORS -eq 0 ]; then
    echo -e "  ${GREEN}Deployment Tests: ALL PASSED${NC}"
    exit 0
else
    echo -e "  ${RED}Deployment Tests: $ERRORS FAILED${NC}"
    exit 1
fi
