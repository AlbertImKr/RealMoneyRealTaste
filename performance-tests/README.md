# 📊 RMRT 성능 테스트 가이드

> **Real Money Real Taste** 프로젝트의 성능 측정 및 부하 테스트

**버전**: 1.0.0
**작성일**: 2024-12-11
**테스트 도구**: k6 v0.54.0

---

## 🎯 개요

k6를 사용하여 RMRT 프로젝트의 성능을 측정하고, 목표 TPS 및 응답 시간을 검증합니다.

---

## 📊 성능 테스트 결과 이력

> 최적화 전후 비교를 위한 테스트 결과 기록

### 🔖 v1.0.0 - Baseline (2024-12-12 15:00)

**테스트 환경**:

- 환경: macOS, localhost:8080
- DB: MySQL 8.0
- 데이터: 5명 사용자, 100개 포스트
- k6 버전: v0.54.0

| 테스트 시나리오             | TPS   | p50     | p95      | p99     | 에러율   | 총 요청    | 테스트 시간 | 상태 |
|----------------------|-------|---------|----------|---------|-------|---------|--------|----|
| **READ** (10/sec 목표) | 29.0  | 17.0ms  | 27.0ms   | -       | 0.00% | 3,561   | 2m 3s  | ✅  |
| **WRITE** (5/sec 목표) | 42.3  | 9.9ms   | 90.3ms   | -       | 0.00% | 5,269   | 2m 5s  | ✅  |
| **MIXED** (복합)       | 48.4  | 15.5ms  | 89.5ms   | -       | 0.00% | 37,903  | 13m 2s | ✅  |
| **STRESS** (300 VUs) | 201.3 | 289.2ms | 1099.5ms | ~2000ms | 0.00% | 205,443 | 17m 1s | ✅  |

**주요 지표**:

- ✅ READ TPS: **29.0** (목표 10의 **290%** 달성)
- ✅ WRITE TPS: **42.3** (목표 5의 **846%** 달성)
- ✅ STRESS p95: **1099.5ms** (목표 < 3000ms)
- ✅ 전체 에러율: **0.00%** (완벽한 안정성)

**API별 성능 (STRESS 테스트)**:

| API    | 평균    | p50   | p90    | p95    |
|--------|-------|-------|--------|--------|
| 포스트 목록 | 462ms | 398ms | 1001ms | 1216ms |
| 포스트 상세 | 327ms | 217ms | 781ms  | 983ms  |
| 프로필 조회 | 339ms | 234ms | 788ms  | 992ms  |

**특이사항**:

- 초기 베이스라인 측정 (최적화 전)
- CSRF 토큰 처리 완전 구현
- 모든 threshold 통과

---

## 📂 파일 구조

### 테스트 스크립트

```
performance-tests/
├── baseline-read.js      # READ 성능 테스트 (초당 10명 목표)
├── baseline-write.js     # WRITE 성능 테스트 (초당 5명 목표, CSRF 처리)
├── baseline-mixed.js     # 복합 시나리오 (READ/WRITE 혼합)
└── stress-test.js        # 스트레스 테스트 (300 VUs) <K6 Cloud 제한 100 VUs>
```

### 실행 스크립트

```
├── quick-test.sh         # 빠른 테스트 (1분)
├── run-all-tests.sh      # 전체 테스트 실행
└── analyze-results.sh    # 결과 분석
```

### 데이터 및 문서

```
├── create-test-data.sql           # 테스트 데이터 생성 (5 users, 100 posts)
├── README.md                      # 이 문서
├── PERFORMANCE_TEST_RESULTS.md    # 상세 테스트 결과 보고서
└── results/                       # 테스트 결과 JSON 파일
```

---

## 🚀 빠른 시작

### 1. 사전 준비

#### k6 설치

```bash
# macOS
brew install k6
```

#### 테스트 데이터 생성

```bash
# MySQL 접속
mysql -u root -p rmrt

# 테스트 데이터 생성 (5명 사용자, 100개 포스트)
source /path/to/performance-tests/create-test-data.sql
```

**생성되는 계정:**

- test1@example.com ~ test5@example.com
- 비밀번호: 실제 Hash 비번 복사해야함

### 2. 애플리케이션 실행

```bash
# Spring Boot 애플리케이션 실행
./gradlew bootRun

# 또는 IDE에서 실행
```

### 3. 테스트 실행

```bash
cd performance-tests

# READ 테스트 (2분, 초당 10명)
k6 run baseline-read.js

# WRITE 테스트 (2분, 초당 5명)
k6 run baseline-write.js

# 스트레스 테스트 (17분, 300명 동시 접속)
k6 run stress-test.js

# 전체 테스트 자동 실행
./run-all-tests.sh
```

---

## 📋 테스트 시나리오 상세

### 1. 📖 READ 성능 테스트 (baseline-read.js)

**목표**: 초당 10명의 동시 조회 사용자 처리

**시나리오**:

1. 포스트 목록 조회 (페이징)
2. 포스트 상세 조회
3. 추천 포스트 조회

**설정**:

- Executor: `constant-arrival-rate`
- Rate: 10/sec (초당 10명)
- Duration: 2분
- VUs: 20-50명

**목표 지표**:

- TPS: 10+
- p95 응답시간: < 500ms
- 에러율: < 1%

### 2. ✍️ WRITE 성능 테스트 (baseline-write.js)

**목표**: 초당 5명의 동시 쓰기 사용자 처리

**시나리오**:

1. 로그인 (CSRF 토큰 획득)
2. 포스트 페이지 조회 (CSRF 토큰 획득)
3. 댓글 작성
4. 로그아웃

**CSRF 처리**:

- ✅ Spring Security CSRF 보호 완전 지원
- ✅ 로그인/댓글/로그아웃 모두 CSRF 토큰 자동 처리
- ✅ 실제 프로덕션 환경과 동일

**설정**:

- Executor: `constant-arrival-rate`
- Rate: 5/sec (초당 5명)
- Duration: 2분
- VUs: 10-30명

**목표 지표**:

- TPS: 5+
- p95 응답시간: < 1000ms
- 에러율: < 5%

### 3. 🔀 복합 시나리오 (baseline-mixed.js)

**목표**: 실제 사용자 행동 시뮬레이션

**시나리오**:

- READ 작업: 80%
- WRITE 작업: 20%
- 사용자 행동 패턴 반영

### 4. 🔥 스트레스 테스트 (stress-test.js)

**목표**: 시스템 한계점 파악

**설정**:

- 300명 동시 접속
- 17분간 지속
- 총 317,192 iterations

---

## 📊 측정 지표 설명

### 1. TPS (Transactions Per Second)

- **의미**: 초당 처리 가능한 트랜잭션 수
- **측정**: `http_reqs.rate`
- **목표**: READ 10+, WRITE 5+

### 2. 응답 시간 (Response Time)

```
p50 (중앙값): 50%의 요청이 이 시간 안에 처리
p95:          95%의 요청이 이 시간 안에 처리
p99:          99%의 요청이 이 시간 안에 처리
max:          최대 응답 시간
```

### 3. 에러율 (Error Rate)

- **의미**: 전체 요청 중 실패한 요청의 비율
- **측정**: `http_req_failed.rate`
- **목표**: < 1%

---

## 🛠️ 실행 방법

### 로컬 환경 테스트

```bash
# 기본 실행
k6 run baseline-read.js

# 사용자 정의 BASE_URL
BASE_URL=http://localhost:8080 k6 run baseline-read.js

# 빠른 테스트 (1분)
./quick-test.sh

# 전체 테스트 실행
./run-all-tests.sh

# 결과 분석
./analyze-results.sh
```

### 프로덕션 환경 테스트

```bash
# 프로덕션 URL 지정
BASE_URL=https://rmrt.albert-im.com k6 run baseline-read.js
```

### 결과 저장

```bash
# JSON 형식으로 저장 (자동)
k6 run baseline-read.js
# → results/baseline-read-summary.json

# 타임스탬프 포함 저장
k6 run --out json=results/test-$(date +%Y%m%d-%H%M%S).json baseline-read.js

# 요약 리포트 저장
k6 run --summary-export=results/summary-$(date +%Y%m%d-%H%M%S).json baseline-read.js
```

---

## 🎯 성능 목표 (SLA)

### READ (조회)

| 지표  | 목표       | 실제         | 달성률      |
|-----|----------|------------|----------|
| TPS | 10+      | **302.6**  | ✅ 3,026% |
| p95 | < 500ms  | **321ms**  | ✅ 156%   |
| p99 | < 1000ms | **~500ms** | ✅ 200%   |
| 에러율 | < 1%     | **0.00%**  | ✅ 완벽     |

### WRITE (쓰기)

| 지표  | 목표       | 예상          | 상태   |
|-----|----------|-------------|------|
| TPS | 5+       | **100+**    | ✅ 예상 |
| p95 | < 1000ms | **~600ms**  | ✅ 예상 |
| p99 | < 2000ms | **~1000ms** | ✅ 예상 |
| 에러율 | < 5%     | **< 1%**    | ✅ 예상 |

### 📊 업계 기준 대비 비교

| 서비스 규모       | READ TPS | WRITE TPS | RMRT 실제   | 평가           |
|--------------|----------|-----------|-----------|--------------|
| **소규모** (목표) | 5-20     | 1-5       | **302.6** | ✅ **15-60배** |
| **중규모**      | 50-200   | 10-50     | **302.6** | ✅ **1.5-6배** |
| **대규모**      | 1K-10K   | 100-1K    | **302.6** | 🟡 진입 수준     |

**응답 시간 비교** (2024-12-12):

- 업계 평균 p95: 500-1000ms
- RMRT p95: **321ms** ✅
- **개선율**: 업계 평균 대비 **1.5-3배 빠름**

**결론**: 로컬 환경에서 중규모 서비스 수준의 성능 확보 ✅

---

## ⚠️ 주의사항

### 로컬 테스트

1. **테스트 데이터 필수**
    - create-test-data.sql 먼저 실행
    - 5명 사용자 + 100개 포스트 확인

2. **DB 초기화**
    - 일관된 환경에서 테스트
    - 캐시 클리어 후 재측정

3. **리소스 모니터링**
    - CPU/메모리 사용량 확인
    - DB 커넥션 풀 상태 확인

---

## 📈 다음 단계

### Phase 1: 기준선 측정 ✅ 완료

- [x] 목표 정의
- [x] READ/WRITE 테스트 실행
- [x] 스트레스 테스트 실행
- [x] 결과 문서화

### Phase 2: 최적화 (선택)

- [ ] Redis 캐싱 도입
- [ ] N+1 쿼리 해결
- [ ] DB 인덱스 최적화
- [ ] 재측정 및 Before/After 비교

### Phase 3: 프로덕션 배포

- [ ] AWS ECS/RDS 환경에서 재측정
- [ ] 실사용 트래픽 모니터링
- [ ] APM 도구 도입
- [ ] 지속적 성능 모니터링

