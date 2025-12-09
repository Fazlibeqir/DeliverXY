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
    let text = await response.text();

    let json;
    try {
        json = text ? JSON.parse(text) : null;
    } catch (e) {
        throw new Error("Server returned non-JSON: " + text.substring(0, 80));
    }
     // 401 → try refresh
     if (response.status === 401) {
        const newToken = await refreshToken();
        if (!newToken) {
            clearToken();
            throw new Error("Session expired. Please login again.");
        }

        headers.Authorization = `Bearer ${newToken}`;
        response = await fetch(url, { ...options, headers });
        text = await response.text();

        try { json = text ? JSON.parse(text) : null; }
        catch { throw new Error("Invalid JSON after refresh"); }
    }

    if (!response.ok) {
        throw new Error(json?.message || json?.error || "API Error");
    }

    return json?.data ?? json;
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
