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
        body: JSON.stringify({ currentLatitude: lat, currentLongitude: lon })
    });
}
