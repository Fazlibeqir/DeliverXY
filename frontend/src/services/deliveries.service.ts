import { apiRequest } from "./api";

export function getNearbyDeliveries(
  latitude: number,
  longitude: number,
  radius = 5
) {
  return apiRequest(
    "GET",
    `/api/deliveries/nearby?latitude=${latitude}&longitude=${longitude}&radius=${radius}`
  );
}

export function assignDelivery(id: number) {
  return apiRequest("POST", `/api/deliveries/${id}/assign`);
}

export function getAssignedDeliveries() {
    return apiRequest("GET", "/api/deliveries/assigned");
  }
  
export function updateDeliveryStatus(id: number, status: string) {
    return apiRequest(
      "PUT",
      `/api/deliveries/${id}/status?status=${status}`
    );
  }
  