#!/bin/bash

# 빠른 성능 테스트 (읽기 전용, 테스트 데이터 불필요)
# 가장 기본적인 읽기 성능만 빠르게 측정합니다.

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_URL="${BASE_URL:-http://localhost:8080}"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

echo -e "${BLUE}=== RMRT 빠른 성능 테스트 ===${NC}"
echo -e "Base URL: ${BASE_URL}"
echo ""

# 서버 연결 확인
echo -e "${YELLOW}서버 연결 확인 중...${NC}"
if curl -sf "${BASE_URL}/" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 서버 연결 성공${NC}"
else
    echo -e "${RED}✗ 서버에 연결할 수 없습니다.${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}읽기 성능 테스트 실행 중...${NC}"
echo ""

# 간단한 읽기 테스트 실행
k6 run \
  --summary-export="results/quick-test-${TIMESTAMP}.json" \
  baseline-read.js

echo ""
echo -e "${GREEN}테스트 완료!${NC}"
echo -e "결과: results/quick-test-${TIMESTAMP}.json"
