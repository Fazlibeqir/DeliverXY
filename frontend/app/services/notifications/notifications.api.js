import { apiFetch } from "../core/api-fetch";

export function getNotifications() {
    return apiFetch("/notifications");
}

export function markNotificationRead(id) {
    return apiFetch(`/notifications/${id}/read`, {
        method: "POST"
    });
}
