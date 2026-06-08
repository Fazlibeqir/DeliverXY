import { setupAuth, basicUserJourney } from './common.js';

export const options = {
  stages: [
    { duration: '20s', target: 5 },
    { duration: '10s', target: 80 },
    { duration: '1m', target: 80 },
    { duration: '10s', target: 5 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.08'],
    http_req_duration: ['p(95)<2000', 'p(99)<4000'],
    deliverxy_api_errors: ['rate<0.08'],
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
