import { Http } from "@nativescript/core";
import { secureStorage, TOKEN_KEYS } from "./secure-storage";

const API_URL = "http://192.168.0.11:8080";

async function getAccessToken() {
    return secureStorage.get({ key: TOKEN_KEYS.access });
}

export async function apiRequest(
    method: string,
    url: string,
    body?: any
) {
    const token = await getAccessToken();

    const response = await Http.request({
        url: API_URL + url,
        method,
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        content: body ? JSON.stringify(body) : undefined,
    });

    if (response.statusCode === 401) {
        throw new Error("UNAUTHORIZED");
    }
    const json = response.content?.toJSON() ?? {};

    if (!json.success) {
        throw new Error(json.message || "API Error");
    }

    return json.data;
}
