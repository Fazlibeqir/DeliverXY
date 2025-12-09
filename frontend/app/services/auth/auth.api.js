import { apiFetchNoAuth, apiFetch } from "../core/api-fetch";
import { saveToken, clearToken, getRefreshToken  } from "../core/auth-store";

export async function login(identifier, password) {
    const data = await apiFetchNoAuth("/auth/login", {
        method: "POST",
        body: JSON.stringify({ identifier, password })
    });

    saveToken(data.accessToken, data.refreshToken);
    return data.user;
}

export async function register(payload) {
    const data = await apiFetchNoAuth("/auth/register", {
        method: "POST",
        body: JSON.stringify(payload)
    });

    saveToken(data.accessToken, data.refreshToken);
    return data.user;
}

export async function logout() {
    try{
        await apiFetch("/auth/logout", {
            method: "POST"
        });
    }catch(_){
        //ignore errors on logout
    }
    clearToken();
}

export async function refreshToken() {
    const refresh = getRefreshToken();
    if (!refresh) return null;

    const data = await apiFetchNoAuth("/auth/refresh", {
        method: "POST",
        body: JSON.stringify({refreshToken: refresh})
    });

    saveToken(data.accessToken, data.refreshToken);
    return data.accessToken;
}

export async function getMe(){
    return apiFetch("/auth/me");
}
