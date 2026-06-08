import { setupAuth, basicUserJourney } from './common.js';

export const options = {
  stages: [
    { duration: '1m', target: 10 },
    { duration: '15m', target: 10 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.03'],
    http_req_duration: ['p(95)<1200', 'p(99)<2500'],
    deliverxy_api_errors: ['rate<0.03'],
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
