import { apiFetch } from "../core/api-fetch";

export function getMyDeliveries() {
    return apiFetch("/deliveries/me");
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
