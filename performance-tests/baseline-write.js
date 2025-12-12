import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';

const errorRate = new Rate('errors');
const loginDuration = new Trend('login_duration');
const commentDuration = new Trend('comment_duration');

export const options = {
    scenarios: {
        constant_load: {
            executor: 'constant-arrival-rate',
            rate: 5,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 10,
            maxVUs: 30,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000', 'p(99)<2000'],
        http_req_failed: ['rate<0.05'],
        errors: ['rate<0.05'],
        http_reqs: ['rate>5'],
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
    const user = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];
    const vuId = __VU;
    const iterationId = __ITER;

    const jar = http.cookieJar();
    jar.clear(BASE_URL);

    // 1. 로그인 페이지에서 CSRF 토큰 획득
    const loginPageRes = http.get(`${BASE_URL}/signin`, {
        tags: {name: 'login_page'},
    });

    if (loginPageRes.status !== 200) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ Login page failed: ${loginPageRes.status}`);
        errorRate.add(1);
        return;
    }

    const loginCsrf = extractCsrfToken(loginPageRes.body);
    if (!loginCsrf) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ CSRF token not found on login page`);
        errorRate.add(1);
        return;
    }

    // 2. 로그인 (CSRF 토큰 포함)
    const loginPayload = `email=${encodeURIComponent(user.email)}&password=${encodeURIComponent(user.password)}&_csrf=${encodeURIComponent(loginCsrf)}`;

    const loginRes = http.post(`${BASE_URL}/signin`, loginPayload, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirects: 5,
        tags: {name: 'login'},
    });

    loginDuration.add(loginRes.timings.duration);

    const loginSuccess = check(loginRes, {
        'Login - status is 200': (r) => r.status === 200,
        'Login - not on signin page': (r) => !r.url.includes('/signin'),
    });

    if (!loginSuccess) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ LOGIN FAILED - ${user.email}, Status: ${loginRes.status}, URL: ${loginRes.url}`);
        errorRate.add(1);
        return;
    }

    sleep(1);

    // 3. 포스트 페이지에서 CSRF 토큰 획득
    const postId = Math.floor(Math.random() * 50) + 1;

    const postPageRes = http.get(`${BASE_URL}/posts/${postId}`, {
        tags: {name: 'post_page'},
    });

    if (postPageRes.status === 404) {
        console.warn(`[VU:${vuId} ITER:${iterationId}] ⚠️ Post ${postId} not found`);
        sleep(3);
        http.get(`${BASE_URL}/signout`, {tags: {name: 'logout'}});
        return;
    }

    if (postPageRes.status !== 200) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ Post page failed: ${postPageRes.status}`);
        errorRate.add(1);
        return;
    }

    const commentCsrf = extractCsrfToken(postPageRes.body);
    if (!commentCsrf) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ CSRF token not found on post page`);
        errorRate.add(1);
        return;
    }

    // 4. 댓글 작성 (CSRF 토큰 포함)
    const commentPayload = `content=${encodeURIComponent(`성능 테스트 댓글 - ${Date.now()}`)}&_csrf=${encodeURIComponent(commentCsrf)}`;

    const commentRes = http.post(`${BASE_URL}/posts/${postId}/comments`, commentPayload, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        tags: {name: 'comment'},
    });

    commentDuration.add(commentRes.timings.duration);

    const commentSuccess = check(commentRes, {
        'Comment - status is 200/201/302': (r) => [200, 201, 302].includes(r.status),
        'Comment - not 403': (r) => r.status !== 403,
    });

    if (!commentSuccess) {
        console.error(`[VU:${vuId} ITER:${iterationId}] ❌ COMMENT FAILED - Post ${postId}, Status: ${commentRes.status}`);
        errorRate.add(1);
        return;
    }

    sleep(2);

    // 5. 로그아웃
    // 로그아웃 테스트 (CSRF 필요)
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

export function setup() {
    console.log(`🚀 Write test against ${BASE_URL}`);
    console.log(`🔐 CSRF enabled mode`);

    const jar = http.cookieJar();
    const testUser = TEST_USERS[0];

    // 로그인 페이지에서 CSRF 토큰 획득
    const loginPageRes = http.get(`${BASE_URL}/signin`);
    const csrfToken = extractCsrfToken(loginPageRes.body);

    console.log(`🔐 CSRF token found: ${csrfToken ? 'Yes' : 'No'}`);

    if (!csrfToken) {
        console.error(`⚠️ CSRF token not found - is CSRF enabled on server?`);
        return {startTime: Date.now(), csrfEnabled: false};
    }

    const loginRes = http.post(
        `${BASE_URL}/signin`,
        `email=${encodeURIComponent(testUser.email)}&password=${encodeURIComponent(testUser.password)}&_csrf=${encodeURIComponent(csrfToken)}`,
        {
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            redirects: 5,
        }
    );

    const success = loginRes.status === 200 && !loginRes.url.includes('/signin');
    console.log(`🔑 Test login: ${testUser.email}`);
    console.log(`   Status: ${loginRes.status}`);
    console.log(`   Success: ${success ? '✅' : '❌'}`);

    return {startTime: Date.now(), csrfEnabled: true};
}

export function teardown(data) {
    console.log(`\n🏁 Completed in ${((Date.now() - data.startTime) / 1000).toFixed(2)}s`);
}

export function handleSummary(data) {
    const m = data.metrics;
    console.log('\n' + '='.repeat(50));
    console.log('📊 SUMMARY (CSRF Enabled)');
    console.log('='.repeat(50));
    if (m.http_req_duration) console.log(`⏱️ p95: ${m.http_req_duration.values['p(95)']?.toFixed(0)}ms`);
    if (m.http_req_failed) console.log(`❌ Errors: ${(m.http_req_failed.values.rate * 100).toFixed(2)}%`);
    if (m.http_reqs) console.log(`📈 RPS: ${m.http_reqs.values.rate?.toFixed(2)}`);

    return {'results/baseline-write-summary.json': JSON.stringify(data, null, 2)};
}
