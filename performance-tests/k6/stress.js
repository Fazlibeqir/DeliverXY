import { setupAuth, basicUserJourney } from './common.js';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '2m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    // Stress tests are allowed to degrade, but they should not collapse immediately.
    http_req_failed: ['rate<0.10'],
    http_req_duration: ['p(95)<2500', 'p(99)<5000'],
    deliverxy_api_errors: ['rate<0.10'],
  },
};

export function setup() {
  return {
    token: setupAuth('CLIENT'),
  };
}

export default function (data) {
  basicUserJourney(data.token);
}
