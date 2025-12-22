#!/bin/bash
#
# PeSIT Wizard Integration Test Suite
# Runs all integration tests for installation, deployment, and transfers
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results
PASSED=0
FAILED=0
SKIPPED=0

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED++))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED++))
}

log_warn() {
    echo -e "${YELLOW}[SKIP]${NC} $1"
    ((SKIPPED++))
}

# Parse arguments
RUN_UNIT=true
RUN_INSTALL=true
RUN_DEPLOY=true
RUN_TRANSFER=true
RUN_CLUSTER=true
CLEANUP=true

while [[ $# -gt 0 ]]; do
    case $1 in
        --unit-only) RUN_INSTALL=false; RUN_DEPLOY=false; RUN_TRANSFER=false; RUN_CLUSTER=false ;;
        --no-unit) RUN_UNIT=false ;;
        --no-install) RUN_INSTALL=false ;;
        --no-deploy) RUN_DEPLOY=false ;;
        --no-transfer) RUN_TRANSFER=false ;;
        --no-cluster) RUN_CLUSTER=false ;;
        --no-cleanup) CLEANUP=false ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  --unit-only     Run only unit tests"
            echo "  --no-unit       Skip unit tests"
            echo "  --no-install    Skip installation tests"
            echo "  --no-deploy     Skip deployment tests"
            echo "  --no-transfer   Skip transfer tests"
            echo "  --no-cluster    Skip cluster tests"
            echo "  --no-cleanup    Don't cleanup after tests"
            exit 0
            ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
    shift
done

echo "========================================"
echo "  PeSIT Wizard Integration Test Suite"
echo "========================================"
echo ""
log_info "Project root: $PROJECT_ROOT"
echo ""

# ============================================
# 1. Unit Tests
# ============================================
if [ "$RUN_UNIT" = true ]; then
    echo "----------------------------------------"
    echo "1. Running Unit Tests"
    echo "----------------------------------------"
    
    # Backend unit tests
    log_info "Running backend unit tests..."
    if cd "$PROJECT_ROOT/pesitwizard-server" && mvn test -q 2>/dev/null; then
        log_success "Backend unit tests passed"
    else
        log_error "Backend unit tests failed"
    fi
    
    # Frontend unit tests
    log_info "Running frontend unit tests..."
    if cd "$PROJECT_ROOT/pesitwizard-client-ui" && npm run test:run 2>/dev/null; then
        log_success "Frontend unit tests passed"
    else
        log_error "Frontend unit tests failed"
    fi
    
    echo ""
fi

# ============================================
# 2. Installation Tests
# ============================================
if [ "$RUN_INSTALL" = true ]; then
    echo "----------------------------------------"
    echo "2. Running Installation Tests"
    echo "----------------------------------------"
    
    if [ -f "$SCRIPT_DIR/test-installation.sh" ]; then
        if bash "$SCRIPT_DIR/test-installation.sh"; then
            log_success "Installation tests passed"
        else
            log_error "Installation tests failed"
        fi
    else
        log_warn "Installation test script not found"
    fi
    
    echo ""
fi

# ============================================
# 3. Deployment Tests
# ============================================
if [ "$RUN_DEPLOY" = true ]; then
    echo "----------------------------------------"
    echo "3. Running Deployment Tests"
    echo "----------------------------------------"
    
    if [ -f "$SCRIPT_DIR/test-deployment.sh" ]; then
        if bash "$SCRIPT_DIR/test-deployment.sh"; then
            log_success "Deployment tests passed"
        else
            log_error "Deployment tests failed"
        fi
    else
        log_warn "Deployment test script not found"
    fi
    
    echo ""
fi

# ============================================
# 4. Transfer Tests
# ============================================
if [ "$RUN_TRANSFER" = true ]; then
    echo "----------------------------------------"
    echo "4. Running Transfer Tests"
    echo "----------------------------------------"
    
    if [ -f "$SCRIPT_DIR/test-transfers.sh" ]; then
        if bash "$SCRIPT_DIR/test-transfers.sh"; then
            log_success "Transfer tests passed"
        else
            log_error "Transfer tests failed"
        fi
    else
        log_warn "Transfer test script not found"
    fi
    
    echo ""
fi

# ============================================
# 5. Cluster Tests
# ============================================
if [ "$RUN_CLUSTER" = true ]; then
    echo "----------------------------------------"
    echo "5. Running Cluster Tests"
    echo "----------------------------------------"
    
    if [ -f "$SCRIPT_DIR/test-cluster.sh" ]; then
        if bash "$SCRIPT_DIR/test-cluster.sh"; then
            log_success "Cluster tests passed"
        else
            log_error "Cluster tests failed"
        fi
    else
        log_warn "Cluster test script not found"
    fi
    
    echo ""
fi

# ============================================
# Summary
# ============================================
echo "========================================"
echo "  Test Results Summary"
echo "========================================"
echo -e "  ${GREEN}Passed:${NC}  $PASSED"
echo -e "  ${RED}Failed:${NC}  $FAILED"
echo -e "  ${YELLOW}Skipped:${NC} $SKIPPED"
echo "========================================"

if [ $FAILED -gt 0 ]; then
    exit 1
else
    exit 0
fi
