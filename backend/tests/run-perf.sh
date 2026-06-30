#!/usr/bin/env bash
set -euo pipefail

APP_NAME="BackendApplication"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPORTS_DIR="${SCRIPT_DIR}/../reports"
TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
OUT_DIR="${REPORTS_DIR}/perf_${TIMESTAMP}"
JFR_FILE="${OUT_DIR}/recording.jfr"
K6_JSON="${OUT_DIR}/k6_report.json"
K6_SUMMARY="${OUT_DIR}/k6_summary.txt"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== WorkHub Performance Test ===${NC}"

# 1. Find Spring Boot PID
PID=$(jps -l 2>/dev/null | grep "${APP_NAME}" | awk '{print $1}' || true)
if [ -z "$PID" ]; then
    echo -e "${RED}Error: ${APP_NAME} not found. Start the app first.${NC}"
    echo "  IntelliJ: Run > BackendApplication"
    echo "  CLI:      EMAIL=... PASSWORD=... ./mvnw spring-boot:run"
    exit 1
fi
echo -e "  App PID: ${YELLOW}${PID}${NC}"

# 2. Validate required env vars
if [ -z "${EMAIL:-}" ] || [ -z "${PASSWORD:-}" ]; then
    echo -e "${RED}Error: EMAIL and PASSWORD env vars required${NC}"
    echo "  Usage: EMAIL=user@test.com PASSWORD=pass $0"
    exit 1
fi

# 3. Create output directory
mkdir -p "$OUT_DIR"

# 4. Validate jcmd available
if ! command -v jcmd &>/dev/null; then
    echo -e "${RED}Error: jcmd not found. Are you using a JDK (not JRE)?${NC}"
    exit 1
fi

# 5. Start JFR recording
echo -e "  Starting JFR recording → ${YELLOW}${JFR_FILE}${NC}"
JFR_OUTPUT=$(jcmd "$PID" JFR.start name=perftest filename="$JFR_FILE" settings=profile 2>&1 || true)
echo "  jcmd: $JFR_OUTPUT"
if echo "$JFR_OUTPUT" | grep -qi "could not\|error\|failed\|exception"; then
    echo -e "${RED}JFR failed to start. Check the app JVM arguments.${NC}"
    echo "  Try: Add -XX:StartFlightRecording=disk=true to your JVM args"
    exit 1
fi

JFR_STOPPED=false
stop_jfr() {
    if [ "$JFR_STOPPED" = true ]; then return; fi
    JFR_STOPPED=true
    echo ""
    echo -e "${YELLOW}Stopping JFR recording...${NC}"
    jcmd "$PID" JFR.stop name=perftest 2>/dev/null || true
}
trap stop_jfr EXIT

# 5. Run k6
K6_SCRIPT="${SCRIPT_DIR}/perf_test.js"
echo -e "  Running k6 (${YELLOW}${K6_SCRIPT}${NC})..."
echo ""

if ! BASE_URL="${BASE_URL:-http://localhost:8080}" \
     EMAIL="$EMAIL" \
     PASSWORD="$PASSWORD" \
     k6 run "$K6_SCRIPT" \
        --out json="$K6_JSON" \
        --summary-export="$K6_SUMMARY" 2>&1; then
    echo -e "${RED}k6 test completed with errors${NC}"
fi

# 6. Stop JFR and print summary (must be in this order for accurate size)
stop_jfr
echo ""
echo -e "${GREEN}=== Performance Test Complete ===${NC}"
echo -e "  Reports: ${YELLOW}${OUT_DIR}${NC}"
echo -e "  JFR:     ${YELLOW}${JFR_FILE}${NC}"
echo -e "  k6 JSON: ${YELLOW}${K6_JSON}${NC}"
JFR_SIZE=$(wc -c < "$JFR_FILE" 2>/dev/null || echo 0)
echo -e "  JFR size: ${YELLOW}$(numfmt --to=iec "$JFR_SIZE" 2>/dev/null || echo "${JFR_SIZE} bytes")${NC}"
