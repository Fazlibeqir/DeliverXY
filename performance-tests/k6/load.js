import { setupAuth, basicUserJourney } from './common.js';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '2m', target: 10 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
    deliverxy_api_errors: ['rate<0.02'],
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
