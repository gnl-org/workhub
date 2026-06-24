import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 }, // Ramp up from 1 to 20 users over 30s
        { duration: '1m', target: 50 },  // Stay steady at 50 users for 1 minute
        { duration: '30s', target: 0 },  // Ramp down to 0 users over 30s
    ],
    thresholds: {
        http_req_duration: ['p(95)<150'], // 95% of requests must complete under 150ms
    },
};

export default function () {
    const url = 'http://localhost:8080/projects';

    // Pass your valid application JWT cookie/headers directly
    const params = {
        headers: {
            'Cookie': 'accessToken=TOKEN_HERE',
            'Content-Type': 'application/json',
        },
    };

    const res = http.get(url, params);

    // Verify that the response status is 200 OK
    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // Paces the user behavior slightly (simulating real user reading time)
    sleep(1);
}