import http from 'k6/http';
import { check, sleep, fail } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<150'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/authenticate`,
        JSON.stringify({
            email: __ENV.EMAIL,
            password: __ENV.PASSWORD,
        }),
        { headers: { 'Content-Type': 'application/json' } },
    );

    if (loginRes.status !== 200) {
        console.error(`Login failed in setup: ${loginRes.status} ${loginRes.body}`);
        fail(`Login failed with status ${loginRes.status}`);
    }

    const jar = http.cookieJar();
    const cookies = jar.cookiesForURL(`${BASE_URL}/`);
    return { token: cookies.accessToken };
}

export default function (data) {
    const jar = http.cookieJar();
    const cookies = jar.cookiesForURL(`${BASE_URL}/`);
    if (!cookies.accessToken) {
        jar.set(`${BASE_URL}/`, 'accessToken', data.token, { path: '/' });
    }

    const res = http.get(`${BASE_URL}/projects`);
    /* 
    Tier 1 (must include):
    - GET /projects — users always land here (already there)
    - GET /projects/{id}/tasks — heaviest query (joins, filters, pagination)
    - PATCH /projects/{id}/tasks/{id}/move — write path + DB constraint checks
    Tier 2 (nice to have):
    - GET /projects/{id}/backlog — data-heavy, multiple stages
    - POST /projects/{id}/tasks — write + DB insert
    - GET /projects/{id}/activity — log aggregation query
    Skip:
    - Auth endpoints (1 call per user, negligible)
    - Config/health endpoints
    - Sprint close (heavy but rare)
    */

    check(res, {
        'projects status is 200': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.error(`/projects returned ${res.status}: ${res.body}`);
    }

    sleep(1);
}
