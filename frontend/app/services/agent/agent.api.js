import { apiFetch } from "../core/api-fetch";

export function getAgentProfile() {
    return apiFetch("/agent/profile");
}

export function updateAgentProfile(body) {
    return apiFetch("/agent/profile", {
        method: "PUT",
        body: JSON.stringify(body)
    });
}


export function updateLocation(lat, lon) {
    return apiFetch("/agent/location", {
        method: "PUT",
        body: JSON.stringify({
            currentLatitude: lat,
            currentLongitude: lon
        })
    });
}

export function getAssignedDeliveries() {
    return apiFetch("/deliveries/assigned");
}
export function assignDelivery(id) {
    return apiFetch(`/deliveries/${id}/assign`, {
        method: "POST"
    });
}
export function updateDeliveryStatus(id, status) {
    return apiFetch(`/deliveries/${id}/status?status=${status}`, {
        method: "PUT"
    });
}
export function updateDeliveryLocation(deliveryId, lat, lon) {
    return apiFetch(`/tracking/${deliveryId}/update`, {
        method: "POST",
        body: JSON.stringify({ lat, lon })
    });
}



