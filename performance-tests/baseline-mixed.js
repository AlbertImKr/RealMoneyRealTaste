import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const readOperations = new Counter('read_operations');
const writeOperations = new Counter('write_operations');
const loginDuration = new Trend('login_duration');
const commentDuration = new Trend('comment_duration');
const postListDuration = new Trend('post_list_duration');
const postDetailDuration = new Trend('post_detail_duration');

export const options = {
    stages: [
        {duration: '1m', target: 20},
        {duration: '3m', target: 50},
        {duration: '5m', target: 50},
        {duration: '1m', target: 100},
        {duration: '2m', target: 50},
        {duration: '1m', target: 0},
    ],
    thresholds: {
        http_req_duration: ['p(95)<800', 'p(99)<1500'],
        http_req_failed: ['rate<0.03'],
        errors: ['rate<0.03'],
        login_duration: ['p(95)<500'],
        comment_duration: ['p(95)<1000'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const TEST_USERS = [
    {email: 'test1@example.com', password: 'Password1!'},
    {email: 'test2@example.com', password: 'Password1!'},
    {email: 'test3@example.com', password: 'Password1!'},
    {email: 'test4@example.com', password: 'Password1!'},
    {email: 'test5@example.com', password: 'Password1!'},
];

// CSRF 토큰 추출 함수
function extractCsrfToken(html) {
    if (!html) return null;

    // <input type="hidden" name="_csrf" value="..." />
    let match = html.match(/name="_csrf"[^>]*value="([^"]+)"/);
    if (match) return match[1];

    // value가 먼저 올 수도 있음
    match = html.match(/value="([^"]+)"[^>]*name="_csrf"/);
    if (match) return match[1];

    // <meta name="_csrf" content="..." />
    match = html.match(/name="_csrf"[^>]*content="([^"]+)"/);
    if (match) return match[1];

    return null;
}

export default function () {
    const isReadOperation = Math.random() < 0.8;

    if (isReadOperation) {
        performReadOperations();
    } else {
        performWriteOperations();
    }
}

// -------------------------------
// 📖 READ OPERATIONS (80%)
// -------------------------------
function performReadOperations() {
    readOperations.add(1);
    const vuId = __VU;

    // 1. 포스트 목록 조회
    let res = http.get(`${BASE_URL}/posts/fragment?page=0&size=10`, {
        tags: {name: 'post_list'},
    });
    postListDuration.add(res.timings.duration);

    if (!check(res, {'Read - Post list 200': (r) => r.status === 200})) {
        console.error(`[VU:${vuId}] ❌ Post list failed: ${res.status}`);
        errorRate.add(1);
    }

    sleep(1);

    // 2. 랜덤 포스트 상세 조회
    const postId = Math.floor(Math.random() * 100) + 1;
    res = http.get(`${BASE_URL}/posts/${postId}`, {
        tags: {name: 'post_detail'},
    });
    postDetailDuration.add(res.timings.duration);

    if (!check(res, {'Read - Post detail ok': (r) => r.status === 200 || r.status === 404})) {
        console.error(`[VU:${vuId}] ❌ Post ${postId} failed: ${res.status}`);
        errorRate.add(1);
    }

    sleep(2);

    // 3. 멤버 프로필 조회
    const memberId = Math.floor(Math.random() * 50) + 4;
    res = http.get(`${BASE_URL}/members/${memberId}`, {
        tags: {name: 'member_profile'},
    });

    if (!check(res, {'Read - Member profile ok': (r) => r.status === 200 || r.status === 404})) {
        console.error(`[VU:${vuId}] ❌ Member ${memberId} failed: ${res.status}`);
        errorRate.add(1);
    }

    sleep(1);
}

// -------------------------------
// ✍️ WRITE OPERATIONS (20%)
// -------------------------------
function performWriteOperations() {
    writeOperations.add(1);
    const vuId = __VU;
    const user = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];

    // 쿠키 초기화
    const jar = http.cookieJar();
    jar.clear(BASE_URL);

    // 1. 로그인 페이지에서 CSRF 토큰 획득
    const loginPageRes = http.get(`${BASE_URL}/signin`, {
        tags: {name: 'login_page'},
    });

    if (loginPageRes.status !== 200) {
        console.error(`[VU:${vuId}] ❌ Login page failed: ${loginPageRes.status}`);
        errorRate.add(1);
        return;
    }

    const loginCsrf = extractCsrfToken(loginPageRes.body);
    if (!loginCsrf) {
        console.error(`[VU:${vuId}] ❌ CSRF token not found on login page`);
        errorRate.add(1);
        return;
    }

    // 2. 로그인 (CSRF 토큰 포함)
    const loginPayload = `email=${encodeURIComponent(user.email)}&password=${encodeURIComponent(user.password)}&_csrf=${encodeURIComponent(loginCsrf)}`;

    const loginRes = http.post(`${BASE_URL}/signin`, loginPayload, {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        redirects: 5,
        tags: {name: 'login'},
    });

    loginDuration.add(loginRes.timings.duration);

    const loginSuccess = check(loginRes, {
        'Write - Login success': (r) => r.status === 200 && !r.url.includes('/signin'),
    });

    if (!loginSuccess) {
        console.error(`[VU:${vuId}] ❌ Login failed: ${user.email}, Status: ${loginRes.status}, URL: ${loginRes.url}`);
        errorRate.add(1);
        return;
    }

    sleep(1);

    // 3. 포스트 페이지에서 CSRF 토큰 획득
    const postId = Math.floor(Math.random() * 50) + 2;

    const postPageRes = http.get(`${BASE_URL}/posts/${postId}`, {
        tags: {name: 'post_page'},
    });

    if (postPageRes.status === 404) {
        console.warn(`[VU:${vuId}] ⚠️ Post ${postId} not found`);
        http.get(`${BASE_URL}/signout`, {tags: {name: 'logout'}});
        sleep(2);
        return;
    }

    if (postPageRes.status !== 200) {
        console.error(`[VU:${vuId}] ❌ Post page failed: ${postPageRes.status}`);
        errorRate.add(1);
        return;
    }

    const commentCsrf = extractCsrfToken(postPageRes.body);
    if (!commentCsrf) {
        console.error(`[VU:${vuId}] ❌ CSRF token not found on post page`);
        errorRate.add(1);
        return;
    }

    // 4. 댓글 작성 (CSRF 토큰 포함)
    const commentPayload = `content=${encodeURIComponent(`부하 테스트 댓글 - VU${vuId} - ${Date.now()}`)}&_csrf=${encodeURIComponent(commentCsrf)}`;

    const commentRes = http.post(`${BASE_URL}/posts/${postId}/comments`, commentPayload, {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        tags: {name: 'comment'},
    });

    commentDuration.add(commentRes.timings.duration);

    const commentSuccess = check(commentRes, {
        'Write - Comment created': (r) => [200, 201, 302].includes(r.status),
    });

    if (!commentSuccess) {
        if (commentRes.status === 403) {
            console.error(`[VU:${vuId}] ❌ Comment 403 - CSRF or Auth failed for post ${postId}`);
            errorRate.add(1);
        } else {
            console.error(`[VU:${vuId}] ❌ Comment failed: Post ${postId}, Status: ${commentRes.status}`);
            errorRate.add(1);
        }
    }

    sleep(2);

    // 5. 로그아웃
    const logoutRes = http.post(
        `${BASE_URL}/signout`,
        `_csrf=${encodeURIComponent(commentCsrf)}`,
        {
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        }
    );

    const logoutSuccess = check(logoutRes, {
        'Logout - status is 200/302': (r) => [200, 302].includes(r.status),
        'Logout - not 403': (r) => r.status !== 403,
    });

    if (!logoutSuccess) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ LOGOUT FAILED - Status: ${logoutRes.status}`);
        errorRate.add(1);
        return;
    }

    sleep(1);
}

// -------------------------------
// 🚀 Setup & Teardown
// -------------------------------
export function setup() {
    console.log(`🚀 Mixed workload test against ${BASE_URL}`);
    console.log(`📊 Read:Write ratio = 80:20`);
    console.log(`🔐 CSRF enabled mode`);

    // 로그인 페이지에서 CSRF 토큰 획득
    const loginPageRes = http.get(`${BASE_URL}/signin`);
    const csrfToken = extractCsrfToken(loginPageRes.body);

    console.log(`🔐 CSRF token found: ${csrfToken ? 'Yes' : 'No'}`);

    if (!csrfToken) {
        console.error(`⚠️ CSRF token not found - is CSRF enabled on server?`);
        return {startTime: Date.now(), csrfEnabled: false};
    }

    // 로그인 테스트
    const testUser = TEST_USERS[0];
    const loginRes = http.post(
        `${BASE_URL}/signin`,
        `email=${encodeURIComponent(testUser.email)}&password=${encodeURIComponent(testUser.password)}&_csrf=${encodeURIComponent(csrfToken)}`,
        {headers: {'Content-Type': 'application/x-www-form-urlencoded'}, redirects: 5}
    );

    const success = loginRes.status === 200 && !loginRes.url.includes('/signin');
    console.log(`🔑 Test login: ${success ? '✅ OK' : '❌ FAILED'} (${testUser.email})`);

    if (!success) {
        console.error(`   Status: ${loginRes.status}, URL: ${loginRes.url}`);
    }

    return {startTime: Date.now(), csrfEnabled: true};
}

export function teardown(data) {
    const duration = ((Date.now() - data.startTime) / 1000 / 60).toFixed(2);
    console.log(`\n🏁 Test completed in ${duration} minutes`);
}

// -------------------------------
// 📊 Summary
// -------------------------------
export function handleSummary(data) {
    const m = data.metrics;

    console.log('\n' + '='.repeat(60));
    console.log('📊 MIXED WORKLOAD TEST SUMMARY (CSRF Enabled)');
    console.log('='.repeat(60));

    // 전체 성능
    if (m.http_req_duration) {
        console.log(`\n⏱️ Overall Response Time:`);
        console.log(`   avg: ${m.http_req_duration.values.avg?.toFixed(0)}ms`);
        console.log(`   p95: ${m.http_req_duration.values['p(95)']?.toFixed(0)}ms`);
        console.log(`   p99: ${m.http_req_duration.values['p(99)']?.toFixed(0)}ms`);
    }

    // 작업별 성능
    if (m.login_duration) {
        console.log(`\n🔑 Login: avg ${m.login_duration.values.avg?.toFixed(0)}ms, p95 ${m.login_duration.values['p(95)']?.toFixed(0)}ms`);
    }
    if (m.comment_duration) {
        console.log(`💬 Comment: avg ${m.comment_duration.values.avg?.toFixed(0)}ms, p95 ${m.comment_duration.values['p(95)']?.toFixed(0)}ms`);
    }
    if (m.post_list_duration) {
        console.log(`📋 Post List: avg ${m.post_list_duration.values.avg?.toFixed(0)}ms, p95 ${m.post_list_duration.values['p(95)']?.toFixed(0)}ms`);
    }
    if (m.post_detail_duration) {
        console.log(`📄 Post Detail: avg ${m.post_detail_duration.values.avg?.toFixed(0)}ms, p95 ${m.post_detail_duration.values['p(95)']?.toFixed(0)}ms`);
    }

    // 처리량
    if (m.read_operations) {
        console.log(`\n📖 Read Operations: ${m.read_operations.values.count}`);
    }
    if (m.write_operations) {
        console.log(`✍️ Write Operations: ${m.write_operations.values.count}`);
    }
    if (m.http_reqs) {
        console.log(`📈 Total RPS: ${m.http_reqs.values.rate?.toFixed(2)}`);
    }

    // 에러율
    if (m.http_req_failed) {
        console.log(`\n❌ HTTP Error Rate: ${(m.http_req_failed.values.rate * 100).toFixed(2)}%`);
    }
    if (m.errors) {
        console.log(`❌ Custom Error Rate: ${(m.errors.values.rate * 100).toFixed(2)}%`);
    }

    console.log('\n' + '='.repeat(60));

    return {
        'results/baseline-mixed-summary.json': JSON.stringify(data, null, 2),
    };
}
