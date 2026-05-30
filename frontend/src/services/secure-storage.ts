import { ApplicationSettings } from "@nativescript/core";
import { SecureStorage } from "@nativescript/secure-storage";

export const TOKEN_KEYS = {
  access: "access_token",
  refresh: "refresh_token",
  expiresAt: "expires_at",
};

const SETTINGS_PREFIX = "secure_";

let nativeSecureStorage: SecureStorage | null = null;

try {
  nativeSecureStorage = new SecureStorage();
} catch {
  nativeSecureStorage = null;
}

function settingsKey(key: string): string {
  return `${SETTINGS_PREFIX}${key}`;
}

async function getNative(key: string): Promise<string | null> {
  if (!nativeSecureStorage) return null;
  try {
    return await nativeSecureStorage.get({ key });
  } catch {
    return null;
  }
}

async function setNative(key: string, value: string): Promise<boolean> {
  if (!nativeSecureStorage) return false;
  try {
    await nativeSecureStorage.set({ key, value });
    return true;
  } catch {
    return false;
  }
}

async function removeNative(key: string): Promise<boolean> {
  if (!nativeSecureStorage) return false;
  try {
    await nativeSecureStorage.remove({ key });
    return true;
  } catch {
    return false;
  }
}

/** SecureStorage with ApplicationSettings fallback (NativeScript Preview / Playground). */
export const secureStorage = {
  async get(options: { key: string }): Promise<string | null> {
    const fromNative = await getNative(options.key);
    if (fromNative != null) return fromNative;
    return ApplicationSettings.getString(settingsKey(options.key));
  },

  async set(options: { key: string; value: string }): Promise<void> {
    const stored = await setNative(options.key, options.value);
    if (!stored) {
      ApplicationSettings.setString(settingsKey(options.key), options.value);
    }
  },

  async remove(options: { key: string }): Promise<void> {
    await removeNative(options.key);
    ApplicationSettings.remove(settingsKey(options.key));
  },
};
