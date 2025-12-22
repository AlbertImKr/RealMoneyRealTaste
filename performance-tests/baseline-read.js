import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const postListDuration = new Trend('post_list_duration');
const postDetailDuration = new Trend('post_detail_duration');
const recommendDuration = new Trend('recommend_duration');

// ============================================================
// 📊 READ 성능 목표
// ============================================================
// 목표: 1초당 10명의 동시 조회 사용자 처리
// - 응답 시간: p95 < 500ms, p99 < 1000ms
// - 에러율: < 1%
// - TPS: 10+ (초당 트랜잭션)
// ============================================================

export const options = {
    scenarios: {
        constant_load: {
            executor: 'constant-arrival-rate',
            rate: 10,                    // 초당 10명의 사용자 시작
            timeUnit: '1s',              // 1초 단위
            duration: '2m',              // 2분간 지속
            preAllocatedVUs: 20,         // 미리 할당할 가상 사용자
            maxVUs: 50,                  // 최대 가상 사용자
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
        errors: ['rate<0.01'],
        http_reqs: ['rate>10'],        // 목표: 초당 10+ 요청
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    // 1. 포스트 목록 조회 (페이징)
    let postListRes = http.get(`${BASE_URL}/posts/fragment?page=0&size=10&sort=createdAt,desc`);
    postListDuration.add(postListRes.timings.duration);

    check(postListRes, {
        'Post List - status is 200': (r) => r.status === 200,
        'Post List - response time < 500ms': (r) => r.timings.duration < 500,
    }) || errorRate.add(1);

    sleep(1);

    // 2. 포스트 상세 조회
    // 실제 존재하는 postId를 사용해야 합니다. 테스트 데이터에 맞게 수정 필요
    const postId = Math.floor(Math.random() * 100) + 1; // 1-100 사이 랜덤 ID
    let postDetailRes = http.get(`${BASE_URL}/posts/${postId}`);
    postDetailDuration.add(postDetailRes.timings.duration);

    check(postDetailRes, {
        'Post Detail - status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        'Post Detail - response time < 500ms': (r) => r.timings.duration < 500,
    }) || errorRate.add(1);

    sleep(1);

    // 3. 추천 포스트 조회
    let recommendRes = http.get(`${BASE_URL}/posts/fragment?page=${Math.floor(Math.random() * 5)}&size=5`);
    recommendDuration.add(recommendRes.timings.duration);

    check(recommendRes, {
        'Recommend - status is 200': (r) => r.status === 200,
        'Recommend - response time < 500ms': (r) => r.timings.duration < 500,
    }) || errorRate.add(1);

    sleep(1); // 사용자가 페이지를 읽는 시간 시뮬레이션
}

export function handleSummary(data) {
    // JSON 요약만 저장 (커스텀 텍스트 요약은 제거)
    return {
        'results/baseline-read-summary.json': JSON.stringify(data),
    };
}
