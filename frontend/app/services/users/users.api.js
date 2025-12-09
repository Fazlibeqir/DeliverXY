import { apiFetch } from "../core/api-fetch";

export function getMe() {
    return apiFetch("/users/me");
}

export function updateMe(body) {
    return apiFetch("/users/me", {
        method: "PUT",
        body: JSON.stringify(body)
    });
}
