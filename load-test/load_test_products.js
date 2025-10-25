import http from 'k6/http';
import { check, sleep } from 'k6'
import { Rate } from 'k6/metrics'

export const options = {
    vus: 5,
    duration: '1m'
}

const BASE_URL= "http://localhost:8000/api/v1"
const PAGE_SIZE= 20

export default function () {
    const url = `${BASE_URL}/products?page=1&size=${PAGE_SIZE}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzdHJpbmciLCJpYXQiOjE3NDI1MjA5MjAsImV4cCI6MTc0Mjc4MDEyMH0.Wpz2n3q4jGqrqJzyJoAiwJXQEPl6lzj7cy3XNWD1GNWNHqnp7itPJZ2Q7UF0IrCvrHvaXWjA4EZ7NbrT36oQLA'
        },
    };

    const response = http.get(url, params);
    check(response, {
        'is status 200': (r) => r.status === 200,
        'rate limit not exceeded': (r) => r.status !== 429
    })

    console.log(`Status: ${response.status}, Time: ${response.timings.duration} ms`)
    sleep(0.1)
}