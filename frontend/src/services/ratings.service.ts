import { apiRequest } from "./api";

export type RatingPayload = {
  deliveryId: number;
  rating: number; // 1-5
  review?: string;
};

export async function createRating(payload: RatingPayload) {
  return apiRequest("POST", "/api/ratings", payload);
}

export async function updateRating(id: number, payload: Omit<RatingPayload, "deliveryId"> & { deliveryId: number }) {
  return apiRequest("PUT", `/api/ratings/${id}`, payload);
}

export async function deleteRating(id: number) {
  return apiRequest("DELETE", `/api/ratings/${id}`);
}

export async function getDeliveryRatings(deliveryId: number) {
  return apiRequest("GET", `/api/ratings/delivery/${deliveryId}`);
}

export async function getUserRatings(userId: number) {
  return apiRequest("GET", `/api/ratings/user/${userId}`);
}

export async function getUserAverage(userId: number) {
  return apiRequest("GET", `/api/ratings/user/${userId}/average`);
}

