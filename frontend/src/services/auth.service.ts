import { apiRequest } from "./api";
import { secureStorage, TOKEN_KEYS } from "./secure-storage";

export async function login(identifier: string, password: string) {
  const res = await apiRequest("POST", "/api/auth/login", {
    identifier,
    password,
  });

  await persistTokens(res);
  return res.user;
}

export async function register(payload: any) {
  const res = await apiRequest("POST", "/api/auth/register", payload);
  await persistTokens(res);
  return res.user;
}

export async function refresh() {
  try {
    const refreshToken = await secureStorage.get({ key: TOKEN_KEYS.refresh });
    if (!refreshToken) throw new Error("NO_REFRESH");

    const res = await apiRequest("POST", "/api/auth/refresh", { refreshToken });
    await persistTokens(res);
    return res.user;
  } catch (e) {
    await logout();
    throw e;
  }
}

async function persistTokens(res: any) {
  const { accessToken, refreshToken, expiresIn } = res;

  if (!accessToken || !refreshToken) {
    throw new Error("Invalid auth response");
  }

  const expiresAt = Date.now() + expiresIn * 1000;

  await secureStorage.set({ key: TOKEN_KEYS.access, value: accessToken });
  await secureStorage.set({ key: TOKEN_KEYS.refresh, value: refreshToken });
  await secureStorage.set({
    key: TOKEN_KEYS.expiresAt,
    value: expiresAt.toString(),
  });
}


export async function logout() {
  try {
    await apiRequest("POST", "/api/auth/logout");
  } catch {}
  await secureStorage.remove({ key: TOKEN_KEYS.access });
  await secureStorage.remove({ key: TOKEN_KEYS.refresh });
  await secureStorage.remove({ key: TOKEN_KEYS.expiresAt });
}
