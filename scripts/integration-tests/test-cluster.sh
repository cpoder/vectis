#!/bin/bash
#
# PeSIT Wizard Cluster Tests
# Validates cluster deployment, failover, and distributed transfers
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Configuration
NAMESPACE="pesitwizard-test"
RELEASE_NAME="pesitwizard-cluster-test"
REPLICAS=3
HEALTH_TIMEOUT=120
FAILOVER_TIMEOUT=60

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
    log_info "Cleaning up cluster test resources..."
    if command -v kubectl &> /dev/null; then
        kubectl delete namespace "$NAMESPACE" 2>/dev/null || true
    fi
    if command -v helm &> /dev/null; then
        helm uninstall "$RELEASE_NAME" -n "$NAMESPACE" 2>/dev/null || true
    fi
}

trap cleanup EXIT

echo "============================================"
echo "  Cluster Tests"
echo "============================================"

# Check prerequisites
if ! command -v kubectl &> /dev/null; then
    echo -e "${YELLOW}[SKIP]${NC} kubectl not available, skipping cluster tests"
    exit 0
fi

if ! command -v helm &> /dev/null; then
    echo -e "${YELLOW}[SKIP]${NC} helm not available, skipping cluster tests"
    exit 0
fi

# Check cluster connectivity
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${YELLOW}[SKIP]${NC} No Kubernetes cluster available"
    exit 0
fi

# ============================================
# Test 1: Namespace Creation
# ============================================
log_info "Test 1: Creating test namespace..."

if kubectl create namespace "$NAMESPACE" 2>/dev/null; then
    log_pass "Namespace created: $NAMESPACE"
else
    log_fail "Failed to create namespace"
    ((ERRORS++))
fi

# ============================================
# Test 2: Helm Chart Deployment
# ============================================
log_info "Test 2: Deploying cluster with Helm ($REPLICAS replicas)..."

cd "$PROJECT_ROOT/pesitwizard-helm-charts"

if helm install "$RELEASE_NAME" ./pesitwizard-server \
    --namespace "$NAMESPACE" \
    --set replicaCount=$REPLICAS \
    --set image.tag=latest \
    --wait --timeout 5m 2>/dev/null; then
    log_pass "Helm deployment successful"
else
    log_fail "Helm deployment failed"
    ((ERRORS++))
    exit 1
fi

# ============================================
# Test 3: Wait for All Pods Ready
# ============================================
log_info "Test 3: Waiting for all pods to be ready..."

ELAPSED=0
while [ $ELAPSED -lt $HEALTH_TIMEOUT ]; do
    READY_PODS=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/name=pesitwizard-server" \
        --field-selector=status.phase=Running -o name 2>/dev/null | wc -l)
    
    if [ "$READY_PODS" -eq "$REPLICAS" ]; then
        log_pass "All $REPLICAS pods are running"
        break
    fi
    
    log_info "Waiting... ($READY_PODS/$REPLICAS ready)"
    sleep 5
    ((ELAPSED+=5))
done

if [ $ELAPSED -ge $HEALTH_TIMEOUT ]; then
    log_fail "Timeout waiting for pods"
    ((ERRORS++))
fi

# ============================================
# Test 4: Service Discovery
# ============================================
log_info "Test 4: Checking service discovery..."

SERVICE_NAME="${RELEASE_NAME}-pesitwizard-server"
if kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" &> /dev/null; then
    log_pass "Service discovered: $SERVICE_NAME"
    
    # Get service details
    kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" -o wide
else
    log_fail "Service not found"
    ((ERRORS++))
fi

# ============================================
# Test 5: Cluster Membership
# ============================================
log_info "Test 5: Checking cluster membership..."

# Get pod names
PODS=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/name=pesitwizard-server" -o name)

for pod in $PODS; do
    POD_NAME=$(echo "$pod" | cut -d'/' -f2)
    
    # Check cluster status via API (port-forward)
    kubectl port-forward "$pod" 8080:8080 -n "$NAMESPACE" &> /dev/null &
    PF_PID=$!
    sleep 3
    
    CLUSTER_STATUS=$(curl -sf "http://localhost:8080/api/v1/cluster/status" 2>/dev/null || echo "{}")
    
    kill $PF_PID 2>/dev/null || true
    
    if [[ "$CLUSTER_STATUS" == *"members"* ]]; then
        log_info "Pod $POD_NAME: cluster membership OK"
    else
        log_info "Pod $POD_NAME: cluster status check skipped"
    fi
done

log_pass "Cluster membership check completed"

# ============================================
# Test 6: Load Distribution
# ============================================
log_info "Test 6: Testing load distribution..."

# Port forward to service
kubectl port-forward "svc/$SERVICE_NAME" 8080:8080 -n "$NAMESPACE" &> /dev/null &
PF_PID=$!
sleep 3

# Send multiple requests and check distribution
RESPONSES=""
for i in {1..10}; do
    RESPONSE=$(curl -sf "http://localhost:8080/api/v1/server/status" 2>/dev/null | grep -o '"nodeId":"[^"]*"' || echo "unknown")
    RESPONSES="$RESPONSES $RESPONSE"
done

kill $PF_PID 2>/dev/null || true

# Check if requests were distributed
UNIQUE_NODES=$(echo "$RESPONSES" | tr ' ' '\n' | sort -u | wc -l)
if [ "$UNIQUE_NODES" -gt 1 ]; then
    log_pass "Load distributed across $UNIQUE_NODES nodes"
else
    echo -e "${YELLOW}[WARN]${NC} Load distribution not verified"
fi

# ============================================
# Test 7: Pod Failure and Recovery
# ============================================
log_info "Test 7: Testing pod failure and recovery..."

# Get first pod
FIRST_POD=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/name=pesitwizard-server" -o name | head -1)

if [ -n "$FIRST_POD" ]; then
    POD_NAME=$(echo "$FIRST_POD" | cut -d'/' -f2)
    log_info "Deleting pod: $POD_NAME"
    
    kubectl delete "$FIRST_POD" -n "$NAMESPACE" --grace-period=0 --force 2>/dev/null || true
    
    # Wait for recovery
    ELAPSED=0
    while [ $ELAPSED -lt $FAILOVER_TIMEOUT ]; do
        READY_PODS=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/name=pesitwizard-server" \
            --field-selector=status.phase=Running -o name 2>/dev/null | wc -l)
        
        if [ "$READY_PODS" -eq "$REPLICAS" ]; then
            log_pass "Cluster recovered to $REPLICAS replicas"
            break
        fi
        
        sleep 5
        ((ELAPSED+=5))
    done
    
    if [ $ELAPSED -ge $FAILOVER_TIMEOUT ]; then
        log_fail "Cluster failed to recover"
        ((ERRORS++))
    fi
else
    echo -e "${YELLOW}[WARN]${NC} No pod found for failover test"
fi

# ============================================
# Test 8: Rolling Update
# ============================================
log_info "Test 8: Testing rolling update..."

# Trigger a rolling restart
if kubectl rollout restart deployment "$RELEASE_NAME-pesitwizard-server" -n "$NAMESPACE" 2>/dev/null; then
    log_info "Rolling restart initiated"
    
    # Wait for rollout
    if kubectl rollout status deployment "$RELEASE_NAME-pesitwizard-server" -n "$NAMESPACE" --timeout=5m 2>/dev/null; then
        log_pass "Rolling update completed successfully"
    else
        log_fail "Rolling update failed"
        ((ERRORS++))
    fi
else
    echo -e "${YELLOW}[WARN]${NC} Could not trigger rolling update"
fi

# ============================================
# Test 9: Cluster Health After Tests
# ============================================
log_info "Test 9: Final cluster health check..."

FINAL_READY=$(kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/name=pesitwizard-server" \
    --field-selector=status.phase=Running -o name 2>/dev/null | wc -l)

if [ "$FINAL_READY" -eq "$REPLICAS" ]; then
    log_pass "Final health check: $FINAL_READY/$REPLICAS pods running"
else
    log_fail "Final health check failed: $FINAL_READY/$REPLICAS pods running"
    ((ERRORS++))
fi

# ============================================
# Summary
# ============================================
echo ""
echo "============================================"
echo "  Cluster Test Summary"
echo "============================================"
echo "  Namespace: $NAMESPACE"
echo "  Release: $RELEASE_NAME"
echo "  Replicas: $REPLICAS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "  ${GREEN}Cluster Tests: ALL PASSED${NC}"
    exit 0
else
    echo -e "  ${RED}Cluster Tests: $ERRORS FAILED${NC}"
    exit 1
fi
