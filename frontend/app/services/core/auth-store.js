import { ApplicationSettings } from "@nativescript/core";

const ACCESS_TOKEN  = "access_token";
const REFRSH_TOKEN = "refresh_token";

export function saveToken(token) {
    ApplicationSettings.setString(ACCESS_TOKEN, token);
    ApplicationSettings.setString(REFRESH_TOKEN, refresh);
}

export function getAccessToken() {
    return ApplicationSettings.getString(ACCESS_TOKEN, null);
}
export function getRefreshToken() {
    return ApplicationSettings.getString(REFRESH_TOKEN, null);
}
export function clearToken() {
    ApplicationSettings.remove(ACCESS_TOKEN);
    ApplicationSettings.remove(REFRESH_TOKEN);
}
