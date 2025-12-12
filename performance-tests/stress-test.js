import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';

const errorRate = new Rate('errors');
const postFragmentDuration = new Trend('post_fragment_duration');
const postDetailDuration = new Trend('post_detail_duration');
const memberProfileDuration = new Trend('member_profile_duration');

// 스트레스 테스트 - 시스템 한계 파악
export const options = {
    stages: [
        {duration: '2m', target: 100},   // 100명까지 증가
        {duration: '3m', target: 100},   // 100명 유지
        {duration: '2m', target: 200},   // 200명까지 증가
        {duration: '3m', target: 200},   // 200명 유지
        {duration: '2m', target: 300},   // 300명까지 증가 (Breaking Point 찾기)
        {duration: '3m', target: 300},   // 300명 유지
        {duration: '2m', target: 0},     // Cool-down
    ],
    thresholds: {
        http_req_duration: ['p(99)<3000'],  // 극한 상황에서도 3초 이내
        http_req_failed: ['rate<0.1'],      // 10% 미만
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const DEBUG = __ENV.DEBUG === 'true';

// 실제 존재하는 데이터 범위 (setup에서 확인 후 설정)
let MAX_POST_ID = 100;
let MAX_MEMBER_ID = 50;

export default function () {
    const vuId = __VU;
    const iterationId = __ITER;

    // 랜덤 엔드포인트 선택
    const endpointType = Math.floor(Math.random() * 3);
    let endpoint, res, duration;

    switch (endpointType) {
        case 0:
            // 포스트 목록
            endpoint = `${BASE_URL}/posts/fragment?page=0&size=10`;
            res = http.get(endpoint, {tags: {name: 'post_fragment'}});
            postFragmentDuration.add(res.timings.duration);
            break;
        case 1:
            // 포스트 상세
            const postId = Math.floor(Math.random() * MAX_POST_ID) + 1;
            endpoint = `${BASE_URL}/posts/${postId}`;
            res = http.get(endpoint, {
                tags: {name: 'post_detail'},
                responseCallback: http.expectedStatuses(200, 404),
            });
            postDetailDuration.add(res.timings.duration);
            break;
        case 2:
            // 멤버 프로필
            const memberId = Math.floor(Math.random() * MAX_MEMBER_ID) + 4;
            endpoint = `${BASE_URL}/members/${memberId}`;
            res = http.get(endpoint, {
                tags: {name: 'member_profile'},
                responseCallback: http.expectedStatuses(200, 404),
            });
            memberProfileDuration.add(res.timings.duration);
            break;
    }

    const success = check(res, {
        'Stress - status ok': (r) => r.status === 200 || r.status === 404,
        'Stress - response time < 3000ms': (r) => r.timings.duration < 3000,
    });

    if (!success) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ FAILED - ${endpoint}, Status: ${res.status}, Duration: ${res.timings.duration.toFixed(0)}ms`);
        errorRate.add(1);
    } else if (DEBUG) {
        console.log(`[VU:${vuId} ITER:${iterationId}] ✅ OK - ${endpoint}, Status: ${res.status}, Duration: ${res.timings.duration.toFixed(0)}ms`);
    }

    // 느린 응답 경고 (1초 이상)
    if (res.timings.duration > 1000) {
        console.warn(`[VU:${vuId} ITER:${iterationId}] ⚠️ SLOW - ${endpoint}, Duration: ${res.timings.duration.toFixed(0)}ms`);
    }

    sleep(0.5);
}

export function setup() {
    console.log(`🚀 Stress test against ${BASE_URL}`);
    console.log(`🐛 DEBUG mode: ${DEBUG ? 'ON' : 'OFF (set DEBUG=true to enable)'}`);

    // 존재하는 포스트 확인
    console.log(`\n📝 Checking existing posts...`);
    let existingPosts = 0;
    for (let i = 1; i <= 10; i++) {
        const res = http.get(`${BASE_URL}/posts/${i}`);
        if (res.status === 200) {
            existingPosts++;
            console.log(`   Post ${i}: ✅`);
        } else {
            console.log(`   Post ${i}: ❌ ${res.status}`);
        }
    }
    console.log(`   Found ${existingPosts}/10 posts in sample`);

    // 존재하는 멤버 확인
    console.log(`\n👤 Checking existing members...`);
    let existingMembers = 0;
    for (let i = 2; i <= 11; i++) {
        const res = http.get(`${BASE_URL}/members/${i}`);
        if (res.status === 200) {
            existingMembers++;
            console.log(`   Member ${i}: ✅`);
        } else {
            console.log(`   Member ${i}: ❌ ${res.status}`);
        }
    }
    console.log(`   Found ${existingMembers}/10 members in sample`);

    // 포스트 목록 확인
    console.log(`\n📋 Checking post fragment...`);
    const fragmentRes = http.get(`${BASE_URL}/posts/fragment?page=0&size=10`);
    console.log(`   Status: ${fragmentRes.status}`);
    if (fragmentRes.status !== 200) {
        console.error(`   ⚠️ Post fragment endpoint may require authentication!`);
    }

    return {startTime: Date.now()};
}

export function teardown(data) {
    const duration = ((Date.now() - data.startTime) / 1000).toFixed(2);
    console.log(`\n🏁 Completed in ${duration}s`);
}

export function handleSummary(data) {
    const m = data.metrics;
    console.log('\n' + '='.repeat(50));
    console.log('📊 STRESS TEST SUMMARY');
    console.log('='.repeat(50));

    if (m.http_req_duration) {
        console.log(`⏱️ Response times:`);
        console.log(`   p50: ${m.http_req_duration.values['p(50)']?.toFixed(0)}ms`);
        console.log(`   p95: ${m.http_req_duration.values['p(95)']?.toFixed(0)}ms`);
        console.log(`   p99: ${m.http_req_duration.values['p(99)']?.toFixed(0)}ms`);
        console.log(`   max: ${m.http_req_duration.values['max']?.toFixed(0)}ms`);
    }

    if (m.http_req_failed) {
        console.log(`❌ HTTP failures: ${(m.http_req_failed.values.rate * 100).toFixed(2)}%`);
    }

    if (m.errors) {
        console.log(`❌ Custom errors: ${(m.errors.values.rate * 100).toFixed(2)}%`);
    }

    if (m.http_reqs) {
        console.log(`📈 RPS: ${m.http_reqs.values.rate?.toFixed(2)}`);
        console.log(`📊 Total requests: ${m.http_reqs.values.count}`);
    }

    // 엔드포인트별 응답시간
    console.log('\n📋 Per-endpoint response times (p95):');
    if (m.post_fragment_duration) {
        console.log(`   post_fragment: ${m.post_fragment_duration.values['p(95)']?.toFixed(0)}ms`);
    }
    if (m.post_detail_duration) {
        console.log(`   post_detail: ${m.post_detail_duration.values['p(95)']?.toFixed(0)}ms`);
    }
    if (m.member_profile_duration) {
        console.log(`   member_profile: ${m.member_profile_duration.values['p(95)']?.toFixed(0)}ms`);
    }

    return {
        'results/stress-test-summary.json': JSON.stringify(data, null, 2),
    };
}
