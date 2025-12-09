import { getAccessToken, getRefreshToken, clearToken } from "./auth-store";

const BASE_URL = "http://10.0.2.2:8080/api";

export async function apiFetch(endpoint, options = {}) {
    const token = getAccessToken();

    const url = BASE_URL + endpoint;

    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

   let response = await fetch(url, {
        ...options,
        headers
    });
    if (response.status === 401) {
        const newToken = await refreshToken();
        if (!newToken) {
            clearToken();
            throw new Error("Session expired. Please log in again.");
        }
        headers.Authorization = `Bearer ${newToken}`;
        response = await fetch(url, {
            ...options,
            headers
        });
    }
    const json = await response.json();
    if (!json.success) {
        throw new Error(json.errorCode || json.message || "API error");
    }
    return json.data;
}
export async function apiFetchNoAuth(endpoint, options = {}) {
    const url = BASE_URL + endpoint;
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    const response = await fetch(url, { ...options, headers });
    const json = await response.json();

    if (!json.success) {
        throw new Error(json.errorCode || json.message || "API Error");
    }

    return json.data;
}
