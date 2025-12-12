#!/bin/bash

# k6 Cloud 리포트 생성 스크립트
# 로컬 테스트 결과를 k6 Cloud에 업로드하여 웹 대시보드에서 확인

set -e

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 기본 설정
BASE_URL="${BASE_URL:-http://localhost:8080}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}k6 Cloud Performance Test Suite${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}Target URL:${NC} $BASE_URL"
echo -e "${GREEN}Timestamp:${NC} $TIMESTAMP"
echo ""

# 서버 헬스 체크
echo -e "${YELLOW}🔍 서버 헬스 체크...${NC}"
if ! curl -f -s -o /dev/null "$BASE_URL/actuator/health" 2>/dev/null; then
    echo -e "${RED}❌ 서버가 응답하지 않습니다: $BASE_URL${NC}"
    echo "서버가 실행 중인지 확인하세요."
    exit 1
fi
echo -e "${GREEN}✅ 서버 정상${NC}"
echo ""

# 테스트 실행 함수
run_cloud_test() {
    local test_name=$1
    local script_file=$2

    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}📊 ${test_name}${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # k6 cloud 명령으로 실행 (자동으로 Cloud에 업로드)
    k6 cloud \
        -e BASE_URL="$BASE_URL" \
        --tag testid="$TIMESTAMP" \
        --tag env="${ENV:-local}" \
        --tag test_name="$test_name" \
        "$script_file"

    local exit_code=$?

    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}✅ ${test_name} 완료${NC}"
    else
        echo -e "${RED}❌ ${test_name} 실패 (exit code: $exit_code)${NC}"
    fi

    echo ""
    return $exit_code
}

# 각 테스트 실행
declare -i total_tests=0
declare -i passed_tests=0
declare -i failed_tests=0

# 1. READ 테스트 (조회 성능)
if [ -f "baseline-read.js" ]; then
    total_tests+=1
    if run_cloud_test "READ 테스트 (조회 성능)" "baseline-read.js"; then
        passed_tests+=1
    else
        failed_tests+=1
    fi
fi

# 2. WRITE 테스트 (쓰기 성능)
if [ -f "baseline-write.js" ]; then
    total_tests+=1
    if run_cloud_test "WRITE 테스트 (쓰기 성능)" "baseline-write.js"; then
        passed_tests+=1
    else
        failed_tests+=1
    fi
fi

# 3. MIXED 테스트 (혼합 워크로드)
if [ -f "baseline-mixed.js" ]; then
    total_tests+=1
    if run_cloud_test "MIXED 테스트 (혼합 워크로드)" "baseline-mixed.js"; then
        passed_tests+=1
    else
        failed_tests+=1
    fi
fi

# 4. STRESS 테스트 (부하 테스트)
if [ -f "stress-test.js" ]; then
    total_tests+=1
    if run_cloud_test "STRESS 테스트 (부하 테스트)" "stress-test.js"; then
        passed_tests+=1
    else
        failed_tests+=1
    fi
fi

# 5. REALISTIC 테스트 (실사용자 시뮬레이션)
if [ -f "prod-realistic.js" ]; then
    total_tests+=1
    if run_cloud_test "REALISTIC 테스트 (실사용자 시뮬레이션)" "prod-realistic.js"; then
        passed_tests+=1
    else
        failed_tests+=1
    fi
fi

# 최종 요약
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}📈 테스트 완료 요약${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✅ 성공:${NC} $passed_tests / $total_tests"
if [ $failed_tests -gt 0 ]; then
    echo -e "${RED}❌ 실패:${NC} $failed_tests / $total_tests"
fi
echo ""
echo -e "${YELLOW}🌐 k6 Cloud에서 결과 확인:${NC}"
echo -e "   https://app.k6.io/"
echo -e "   (Tag 필터: testid=$TIMESTAMP)"
echo ""

if [ $failed_tests -eq 0 ]; then
    echo -e "${GREEN}🎉 모든 테스트 성공!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  일부 테스트 실패${NC}"
    exit 1
fi
