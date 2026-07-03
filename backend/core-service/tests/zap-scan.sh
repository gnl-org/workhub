#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPORTS_DIR="${SCRIPT_DIR}/../reports"
TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
OUT_DIR="${REPORTS_DIR}/zap_${TIMESTAMP}"
HTML_REPORT="${OUT_DIR}/report.html"
ZAP_IMAGE="ghcr.io/zaproxy/zaproxy:stable"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== WorkHub OWASP ZAP Security Scan ===${NC}"

# 1. Validate Docker
if ! command -v docker &>/dev/null; then
    echo -e "${RED}Error: Docker not found.${NC}"
    exit 1
fi

# 2. Validate required env vars
if [ -z "${EMAIL:-}" ] || [ -z "${PASSWORD:-}" ]; then
    echo -e "${RED}Error: EMAIL and PASSWORD env vars required${NC}"
    echo "  Usage: EMAIL=user@example.com PASSWORD=your-password bash backend/core-service/tests/zap-scan.sh"
    exit 1
fi

# 3. Login via curl and extract accessToken
echo -e "  Logging in as ${YELLOW}${EMAIL}${NC}..."
LOGIN_RES=$(curl -s -c - -X POST http://localhost:8080/api/v1/auth/authenticate \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}" || true)
TOKEN=$(echo "$LOGIN_RES" | grep accessToken | awk '{print $NF}')

if [ -z "$TOKEN" ]; then
    echo -e "${RED}Login failed. Check credentials or ensure the app is running.${NC}"
    echo "  Login response: $LOGIN_RES"
    exit 1
fi
echo -e "  Token acquired: ${YELLOW}${TOKEN:0:20}...${NC}"

# 4. Check app is running locally
if ! curl -sf http://localhost:8080/health >/dev/null 2>&1; then
    echo -e "${RED}App not reachable on localhost:8080. Is it running?${NC}"
    exit 1
fi

# 5. Create output directory
mkdir -p "$OUT_DIR"

# 6. Ensure image is available (pull only if not cached)
if ! docker image inspect "$ZAP_IMAGE" >/dev/null 2>&1; then
    echo -e "  Pulling ${ZAP_IMAGE}..."
    docker pull "$ZAP_IMAGE" >/dev/null 2>&1
fi

# 7. Run ZAP scan
echo -e "  Starting ZAP baseline scan (may take a few minutes)..."
echo ""
docker run --rm \
    -v "${OUT_DIR}:/zap/wrk/:rw" \
    -e ZAP_AUTH_HEADER="Cookie" \
    -e ZAP_AUTH_HEADER_VALUE="accessToken=${TOKEN}" \
    "$ZAP_IMAGE" zap-baseline.py \
        -t http://host.docker.internal:8080/projects \
        -r report.html 2>&1

# 7. Print summary
ZAP_EXIT=$?
echo ""
echo -e "${GREEN}=== ZAP Scan Complete ===${NC}"
echo -e "  Reports: ${YELLOW}${OUT_DIR}${NC}"
echo -e "  HTML:    ${YELLOW}${HTML_REPORT}${NC}"
if [ -f "$HTML_REPORT" ]; then
    echo -e "  Size:    ${YELLOW}$(wc -c < "$HTML_REPORT") bytes${NC}"
fi
echo -e "  Exit:    ${YELLOW}${ZAP_EXIT}${NC}"
echo ""
echo -e "  Open the HTML report in a browser to review findings."
