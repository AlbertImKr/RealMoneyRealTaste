#!/bin/bash

# RMRT 성능 테스트 실행 스크립트
# 모든 성능 테스트를 순차적으로 실행하고 결과를 저장합니다.

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본 설정
BASE_URL="${BASE_URL:-http://localhost:8080}"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
RESULTS_DIR="results/${TIMESTAMP}"

echo -e "${BLUE}=== RMRT 성능 테스트 시작 ===${NC}"
echo -e "Base URL: ${BASE_URL}"
echo -e "Timestamp: ${TIMESTAMP}"
echo ""

# 결과 디렉토리 생성
mkdir -p "${RESULTS_DIR}"

# 서버 헬스 체크
echo -e "${YELLOW}서버 연결 확인 중...${NC}"
if curl -sf "${BASE_URL}/actuator/health" > /dev/null 2>&1 || curl -sf "${BASE_URL}/" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 서버 연결 성공${NC}"
else
    echo -e "${RED}✗ 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인하세요.${NC}"
    echo -e "  URL: ${BASE_URL}"
    exit 1
fi

echo ""

# 1. 읽기 성능 테스트
echo -e "${BLUE}1/4: 읽기 성능 테스트 실행 중...${NC}"
k6 run \
  -e BASE_URL="${BASE_URL}" \
  --out "json=${RESULTS_DIR}/baseline-read.json" \
  --summary-export="${RESULTS_DIR}/baseline-read-summary.json" \
  baseline-read.js

echo -e "${GREEN}✓ 읽기 성능 테스트 완료${NC}"
echo ""

# 2. 쓰기 성능 테스트
echo -e "${BLUE}2/4: 쓰기 성능 테스트 실행 중...${NC}"
echo -e "${YELLOW}주의: 테스트 계정이 필요합니다 (test1@example.com ~ test5@example.com)${NC}"
k6 run \
  -e BASE_URL="${BASE_URL}" \
  --out "json=${RESULTS_DIR}/baseline-write.json" \
  --summary-export="${RESULTS_DIR}/baseline-write-summary.json" \
  baseline-write.js

echo -e "${GREEN}✓ 쓰기 성능 테스트 완료${NC}"
echo ""

# 3. 복합 시나리오 테스트
echo -e "${BLUE}3/4: 복합 시나리오 테스트 실행 중...${NC}"
k6 run \
  -e BASE_URL="${BASE_URL}" \
  --out "json=${RESULTS_DIR}/baseline-mixed.json" \
  --summary-export="${RESULTS_DIR}/baseline-mixed-summary.json" \
  baseline-mixed.js

echo -e "${GREEN}✓ 복합 시나리오 테스트 완료${NC}"
echo ""

# 4. 스트레스 테스트
echo -e "${BLUE}4/4: 스트레스 테스트 실행 중...${NC}"
echo -e "${YELLOW}주의: 고부하 테스트입니다. 프로덕션 환경에서는 신중하게 실행하세요.${NC}"
read -p "계속하시겠습니까? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    k6 run \
      -e BASE_URL="${BASE_URL}" \
      --out "json=${RESULTS_DIR}/stress-test.json" \
      --summary-export="${RESULTS_DIR}/stress-test-summary.json" \
      stress-test.js
    echo -e "${GREEN}✓ 스트레스 테스트 완료${NC}"
else
    echo -e "${YELLOW}스트레스 테스트를 건너뜁니다.${NC}"
fi

echo ""
echo -e "${GREEN}=== 모든 성능 테스트 완료 ===${NC}"
echo -e "결과 저장 위치: ${RESULTS_DIR}"
echo ""
echo -e "${BLUE}다음 단계:${NC}"
echo "1. 결과 파일 확인: ls -lh ${RESULTS_DIR}/"
echo "2. 요약 보고서 생성: cat ${RESULTS_DIR}/*-summary.json"
echo "3. 성능 분석 문서 작성"
echo ""
