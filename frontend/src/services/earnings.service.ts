import { apiRequest } from "./api";

export function getSummary(start?: string, end?: string) {
  const qs = new URLSearchParams();
  if (start) qs.set("start", start);
  if (end) qs.set("end", end);
  const suffix = qs.toString() ? `/summary?${qs.toString()}` : "/summary";
  return apiRequest("GET", `/api/earnings${suffix}`);
}

export function getEarnings(page = 0, size = 10) {
  return apiRequest("GET", `/api/earnings?page=${page}&size=${size}`);
}

export function requestPayout(payload: {
  periodStart?: string;
  periodEnd?: string;
  paymentMethod?: string;
  destination?: string;
}) {
  return apiRequest("POST", "/api/earnings/request-payout", payload);
}

export function getPayoutHistory(page = 0, size = 10) {
  return apiRequest("GET", `/api/earnings/payouts?page=${page}&size=${size}`);
}
