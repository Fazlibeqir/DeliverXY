import { setupAuth, basicUserJourney } from './common.js';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500'],
    deliverxy_api_errors: ['rate<0.05'],
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
