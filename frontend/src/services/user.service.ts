import { apiRequest } from "./api";

export async function getMe() {
  return apiRequest("GET", "/api/users/me");
}

export async function updateMe(payload: {
  phoneNumber?: string;
  firstName?: string;
  lastName?: string;
}) {
  return apiRequest("PUT", "/api/users/me", payload);
}

