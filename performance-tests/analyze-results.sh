#!/bin/bash

# 성능 테스트 결과 분석 스크립트

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# 결과 파일 찾기 (디렉토리 또는 직접 파일)
if [ -d "results" ]; then
    # 디렉토리 내 최신 JSON 파일 찾기
    LATEST_FILE=$(ls -t results/*-summary.json 2>/dev/null | head -1)

    # 하위 디렉토리도 확인
    if [ -z "$LATEST_FILE" ]; then
        LATEST_FILE=$(ls -t results/*/*-summary.json 2>/dev/null | head -1)
    fi
fi

if [ -z "$LATEST_FILE" ] || [ ! -f "$LATEST_FILE" ]; then
    echo -e "${RED}결과 파일을 찾을 수 없습니다.${NC}"
    echo "먼저 성능 테스트를 실행하세요: k6 run baseline-mixed.js"
    exit 1
fi

echo -e "${BLUE}=== 성능 테스트 결과 분석 ===${NC}"
echo -e "결과 파일: ${LATEST_FILE}"
echo ""

# jq 설치 확인
if ! command -v jq &> /dev/null; then
    echo -e "${RED}jq가 설치되어 있지 않습니다.${NC}"
    echo "설치: brew install jq (Mac) / apt install jq (Linux)"
    exit 1
fi

# JSON 구조 확인 (metrics가 직접 있는지, values 안에 있는지)
HAS_VALUES=$(jq -r '.metrics.http_req_duration | has("values")' "$LATEST_FILE" 2>/dev/null || echo "false")

if [ "$HAS_VALUES" == "true" ]; then
    # 구조: metrics.http_req_duration.values.avg
    AVG_PATH=".metrics.http_req_duration.values.avg"
    P95_PATH=".metrics.http_req_duration.values[\"p(95)\"]"
    P99_PATH=".metrics.http_req_duration.values[\"p(99)\"]"
    COUNT_PATH=".metrics.http_reqs.values.count"
    RATE_PATH=".metrics.http_reqs.values.rate"
    FAIL_PATH=".metrics.http_req_failed.values.rate"
    VUS_PATH=".metrics.vus.values.max"
else
    # 구조: metrics.http_req_duration.avg (직접)
    AVG_PATH=".metrics.http_req_duration.avg"
    P95_PATH=".metrics.http_req_duration[\"p(95)\"]"
    P99_PATH=".metrics.http_req_duration[\"p(99)\"]"
    COUNT_PATH=".metrics.http_reqs.count"
    RATE_PATH=".metrics.http_reqs.rate"
    FAIL_PATH=".metrics.http_req_failed.rate"
    VUS_PATH=".metrics.vus.max"
fi

echo -e "${GREEN}📊 HTTP 응답 시간${NC}"
AVG=$(jq -r "$AVG_PATH // 0" "$LATEST_FILE" 2>/dev/null)
P95=$(jq -r "$P95_PATH // 0" "$LATEST_FILE" 2>/dev/null)
P99=$(jq -r "$P99_PATH // 0" "$LATEST_FILE" 2>/dev/null)

printf "  평균: %.2fms\n" "$AVG"
printf "  p95:  %.2fms\n" "$P95"
printf "  p99:  %.2fms\n" "$P99"
echo ""

echo -e "${GREEN}⚡ 처리량${NC}"
COUNT=$(jq -r "$COUNT_PATH // 0" "$LATEST_FILE" 2>/dev/null)
RATE=$(jq -r "$RATE_PATH // 0" "$LATEST_FILE" 2>/dev/null)

echo "  총 요청: $COUNT"
printf "  TPS:     %.2f\n" "$RATE"
echo ""

echo -e "${GREEN}❌ 에러율${NC}"
FAIL_RATE=$(jq -r "$FAIL_PATH // 0" "$LATEST_FILE" 2>/dev/null)
FAIL_PERCENT=$(echo "$FAIL_RATE * 100" | bc -l 2>/dev/null || echo "0")
printf "  HTTP 실패율: %.2f%%\n" "$FAIL_PERCENT"

# 커스텀 에러율도 확인
if [ "$HAS_VALUES" == "true" ]; then
    CUSTOM_ERR=$(jq -r ".metrics.errors.values.rate // 0" "$LATEST_FILE" 2>/dev/null)
else
    CUSTOM_ERR=$(jq -r ".metrics.errors.rate // 0" "$LATEST_FILE" 2>/dev/null)
fi
CUSTOM_PERCENT=$(echo "$CUSTOM_ERR * 100" | bc -l 2>/dev/null || echo "0")
printf "  커스텀 에러율: %.2f%%\n" "$CUSTOM_PERCENT"
echo ""

echo -e "${GREEN}👥 가상 사용자${NC}"
VUS_MAX=$(jq -r "$VUS_PATH // 0" "$LATEST_FILE" 2>/dev/null)
echo "  최대: $VUS_MAX"
echo ""

# API별 성능 (있는 경우만)
echo -e "${GREEN}🔍 API별 성능${NC}"
API_METRICS=("login_duration:로그인" "comment_duration:댓글" "post_list_duration:포스트목록" "post_detail_duration:포스트상세")

for metric in "${API_METRICS[@]}"; do
    KEY="${metric%%:*}"
    LABEL="${metric##*:}"

    if [ "$HAS_VALUES" == "true" ]; then
        API_AVG=$(jq -r ".metrics.${KEY}.values.avg // empty" "$LATEST_FILE" 2>/dev/null)
        API_P95=$(jq -r ".metrics.${KEY}.values[\"p(95)\"] // empty" "$LATEST_FILE" 2>/dev/null)
    else
        API_AVG=$(jq -r ".metrics.${KEY}.avg // empty" "$LATEST_FILE" 2>/dev/null)
        API_P95=$(jq -r ".metrics.${KEY}[\"p(95)\"] // empty" "$LATEST_FILE" 2>/dev/null)
    fi

    if [ -n "$API_AVG" ] && [ "$API_AVG" != "null" ]; then
        printf "  %-12s avg: %6.2fms, p95: %6.2fms\n" "$LABEL" "$API_AVG" "$API_P95"
    fi
done
echo ""

# 작업 카운터
echo -e "${GREEN}📋 작업 통계${NC}"
if [ "$HAS_VALUES" == "true" ]; then
    READ_OPS=$(jq -r ".metrics.read_operations.values.count // 0" "$LATEST_FILE" 2>/dev/null)
    WRITE_OPS=$(jq -r ".metrics.write_operations.values.count // 0" "$LATEST_FILE" 2>/dev/null)
else
    READ_OPS=$(jq -r ".metrics.read_operations.count // 0" "$LATEST_FILE" 2>/dev/null)
    WRITE_OPS=$(jq -r ".metrics.write_operations.count // 0" "$LATEST_FILE" 2>/dev/null)
fi
echo "  Read 작업:  $READ_OPS"
echo "  Write 작업: $WRITE_OPS"
echo ""

# Threshold 결과
echo -e "${GREEN}📏 Threshold 결과${NC}"
THRESHOLDS=$(jq -r '.thresholds // empty' "$LATEST_FILE" 2>/dev/null)
if [ -n "$THRESHOLDS" ] && [ "$THRESHOLDS" != "null" ]; then
    jq -r '.thresholds | to_entries[] | "  \(if .value.ok then "✅" else "❌" end) \(.key)"' "$LATEST_FILE" 2>/dev/null
else
    echo "  Threshold 데이터 없음"
fi
echo ""

# 평가
echo -e "${BLUE}🎯 종합 평가${NC}"
echo ""

# 응답 시간 평가
if (( $(echo "$P95 < 100" | bc -l) )); then
    echo -e "  응답 시간: ${GREEN}🟢 우수${NC} (p95: ${P95}ms)"
elif (( $(echo "$P95 < 300" | bc -l) )); then
    echo -e "  응답 시간: ${GREEN}🟢 양호${NC} (p95: ${P95}ms)"
elif (( $(echo "$P95 < 500" | bc -l) )); then
    echo -e "  응답 시간: ${YELLOW}🟡 보통${NC} (p95: ${P95}ms)"
elif (( $(echo "$P95 < 1000" | bc -l) )); then
    echo -e "  응답 시간: ${YELLOW}🟠 주의${NC} (p95: ${P95}ms)"
else
    echo -e "  응답 시간: ${RED}🔴 개선 필요${NC} (p95: ${P95}ms)"
fi

# TPS 평가
if (( $(echo "$RATE > 100" | bc -l) )); then
    echo -e "  처리량:    ${GREEN}🟢 우수${NC} (TPS: ${RATE})"
elif (( $(echo "$RATE > 50" | bc -l) )); then
    echo -e "  처리량:    ${GREEN}🟢 양호${NC} (TPS: ${RATE})"
elif (( $(echo "$RATE > 20" | bc -l) )); then
    echo -e "  처리량:    ${YELLOW}🟡 보통${NC} (TPS: ${RATE})"
else
    echo -e "  처리량:    ${RED}🔴 개선 필요${NC} (TPS: ${RATE})"
fi

# 안정성 평가
if (( $(echo "$FAIL_RATE == 0 && $CUSTOM_ERR == 0" | bc -l) )); then
    echo -e "  안정성:    ${GREEN}🟢 완벽${NC} (에러 0%)"
elif (( $(echo "$FAIL_RATE < 0.01" | bc -l) )); then
    echo -e "  안정성:    ${GREEN}🟢 우수${NC} (에러율 < 1%)"
elif (( $(echo "$FAIL_RATE < 0.03" | bc -l) )); then
    echo -e "  안정성:    ${YELLOW}🟡 양호${NC} (에러율 < 3%)"
else
    echo -e "  안정성:    ${RED}🔴 개선 필요${NC} (에러율: ${FAIL_PERCENT}%)"
fi

echo ""
echo -e "${BLUE}---${NC}"
echo -e "${BLUE}💡 상세 확인: cat $LATEST_FILE | jq${NC}"
echo ""
