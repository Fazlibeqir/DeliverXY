import { SecureStorage } from "@nativescript/secure-storage";

export const secureStorage = new SecureStorage();

export const TOKEN_KEYS = {
  access: "access_token",
  refresh: "refresh_token",
  expiresAt: "expires_at",
};
