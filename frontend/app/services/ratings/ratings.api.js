import { apiFetch } from "../core/api-fetch";

export function rateDelivery(body) {
    return apiFetch("/ratings", {
        method: "POST",
        body: JSON.stringify(body)
    });
}

export function getRatingsByDelivery(deliveryId) {
    return apiFetch(`/ratings/delivery/${deliveryId}`);
}
