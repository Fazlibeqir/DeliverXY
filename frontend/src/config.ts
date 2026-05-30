// Backend URLs — webpack picks the right one per command:
//   ns preview     → NS_PREVIEW=true  → PREVIEW_API_URL HTTPS/ngrok
//   ns run android → NS_PREVIEW=false → ANDROID_API_URL HTTP/EC2

const ANDROID_API_URL =
  (process.env.ANDROID_API_URL as string) || "http://13.60.157.179:8080";

const PREVIEW_API_URL =
  (process.env.PREVIEW_API_URL as string) || "https://55c8-92-53-31-13.ngrok-free.app";

/**
 * Injected at build time:
 * true  for `ns preview`
 * false for `ns run android`
 */
export const USE_WEB_MAP = process.env.NS_PREVIEW === "true";

export const API_URL = USE_WEB_MAP
  ? ((process.env.API_URL as string) || PREVIEW_API_URL)
  : ANDROID_API_URL;

const getRequiredEnv = (key: string): string => {
  const value = process.env[key] as string | undefined;

  if (!value) {
    throw new Error(`Missing required environment variable: ${key}`);
  }

  return value;
};

export const MAPBOX_ACCESS_TOKEN = getRequiredEnv("MAPBOX_ACCESS_TOKEN");

console.log("=== API CONFIGURATION ===");
console.log("Mode:", USE_WEB_MAP ? "Preview WebView map" : "Native Mapbox");
console.log("API_URL:", API_URL);
console.log("========================");

export const isLocalEnv = (): boolean => false;

export const isAwsEnv = (): boolean => true;