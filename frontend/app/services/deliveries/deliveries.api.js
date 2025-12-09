import { apiFetch } from "../core/api-fetch";

// CLIENT deliveries
export function getMyDeliveries() {
  return apiFetch("/deliveries/mine");
}

// AGENT deliveries
export function getAssignedDeliveries() {
  return apiFetch("/deliveries/assigned");
}
// Delivery details
export function getDelivery(id) {
  return apiFetch(`/deliveries/${id}`);
}

export function createDelivery(body) {
    return apiFetch("/deliveries", {
        method: "POST",
        body: JSON.stringify(body)
    });
}

export function updateDeliveryLocation(deliveryId, lat, lon) {
    return apiFetch(`/deliveries/${deliveryId}/location`, {
        method: "PUT",
        body: JSON.stringify({ lat, lon })
    });
}
