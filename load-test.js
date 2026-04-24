import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metric to track error rate
const errorRate = new Rate('errors');

export const options = {
    stages: [
        { duration: '30s', target: 10 },  // Ramp up to 10 users over 30 seconds
        { duration: '1m', target: 50 },   // Ramp up to 50 users over 1 minute
        { duration: '30s', target: 0 },   // Ramp down to 0 users
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% of requests must complete within 500ms
        errors: ['rate<0.01'],             // Error rate must be below 1%
    },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {

    // Test 1 — Movie listing (public endpoint, no auth needed)
    const moviesRes = http.get(`${BASE_URL}/movies`);
    check(moviesRes, {
        'movies status is 200': (r) => r.status === 200,
        'movies response time < 500ms': (r) => r.timings.duration < 500,
    });
    errorRate.add(moviesRes.status !== 200);

    sleep(0.5);

    // Test 2 — Seat availability (cached by Redis)
    const seatsRes = http.get(`${BASE_URL}/seats/showtime/2/all`);
    check(seatsRes, {
        'seats status is 200': (r) => r.status === 200,
        'seats response time < 500ms': (r) => r.timings.duration < 500,
    });
    errorRate.add(seatsRes.status !== 200);

    sleep(0.5);

    // Test 3 — Showtime listing (public endpoint)
    const showtimesRes = http.get(`${BASE_URL}/showtimes/movie/2`);
    check(showtimesRes, {
        'showtimes status is 200': (r) => r.status === 200,
        'showtimes response time < 500ms': (r) => r.timings.duration < 500,
    });
    errorRate.add(showtimesRes.status !== 200);

    sleep(1);
}