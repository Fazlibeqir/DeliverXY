import { Http, ImageSource, knownFolders, path as nsPath, File } from "@nativescript/core";
import { secureStorage, TOKEN_KEYS } from "./secure-storage";

export const API_URL = "http://192.168.0.11:8080";

export function toAbsoluteUrl(path: string | null | undefined): string | undefined {
    if (!path) return undefined;
    if (path.startsWith("http://") || path.startsWith("https://")) return path;
    if (path.startsWith("/")) return API_URL + path;
    return path;
}

async function getAccessToken() {
    return secureStorage.get({ key: TOKEN_KEYS.access });
}

const imageCache = new Map<string, string>();

export async function getAuthenticatedImageUrl(imageUrl: string | null | undefined): Promise<string | undefined> {
    if (!imageUrl) return undefined;
    
    const absoluteUrl = toAbsoluteUrl(imageUrl);
    if (!absoluteUrl) return undefined;

    if (imageCache.has(absoluteUrl)) {
        const cached = imageCache.get(absoluteUrl)!;
        const filePath = cached.replace(/^file:\/\//, '');
        try {
            const exists = ImageSource.fromFileSync(filePath);
            if (exists) return cached;
        } catch {
            imageCache.delete(absoluteUrl);
        }
    }
    
    try {
        const token = await getAccessToken();

        const response = await Http.request({
            url: absoluteUrl,
            method: "GET",
            headers: {
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
        });
        
        if (response.statusCode !== 200) {
            console.error(`Failed to download image: ${response.statusCode}`);
            return undefined;
        }
        
        const content = response.content;
        if (!content) {
            console.error("No content in response");
            return undefined;
        }

        const cacheFolder = knownFolders.documents();
        const fileName = `img_${Date.now()}_${Math.random().toString(36).substring(7)}.jpg`;
        const filePath = nsPath.join(cacheFolder.path, fileName);

        let imageSource: ImageSource | null = null;

        try {
            const token = await getAccessToken();
            if (token) {
                const urlWithToken = `${absoluteUrl}${absoluteUrl.includes('?') ? '&' : '?'}token=${encodeURIComponent(token)}`;
                try {
                    const result = await Http.getImage(urlWithToken);
                    imageSource = result;
                } catch {}
            }
        } catch (e) {}

        if (!imageSource) {
            try {
                const file = File.fromPath(filePath);

                try {
                    const rawBytes = (content as any).raw || (content as any).native;
                    if (rawBytes) {
                        if (rawBytes instanceof ArrayBuffer) {
                            const bytes = new Uint8Array(rawBytes);
                            file.writeSync(bytes);
                        } else if (rawBytes instanceof Uint8Array) {
                            file.writeSync(rawBytes);
                        } else if (typeof rawBytes === 'string') {
                            try {
                                const decoded = atob(rawBytes);
                                const bytes = new Uint8Array(decoded.length);
                                for (let i = 0; i < decoded.length; i++) {
                                    bytes[i] = decoded.charCodeAt(i);
                                }
                                file.writeSync(bytes);
                            } catch {
                                file.writeTextSync(rawBytes);
                            }
                        } else {
                            file.writeSync(rawBytes);
                        }

                        const result = ImageSource.fromFileSync(filePath);
                        imageSource = result instanceof Promise ? await result : (result as unknown as ImageSource);
                    }
                } catch (e1) {}

                if (!imageSource && (content as any).toBase64String) {
                    try {
                        const base64 = (content as any).toBase64String();
                        const result = ImageSource.fromBase64(base64);
                        imageSource = result instanceof Promise ? await result : (result as unknown as ImageSource);
                    } catch (e2) {}
                }

                if (!imageSource) {
                    try {
                        const str = content.toString();
                        if (str && str.length > 0) {
                            try {
                                const result = ImageSource.fromBase64(str);
                                imageSource = result instanceof Promise ? await result : (result as unknown as ImageSource);
                            } catch {
                                try {
                                    file.writeTextSync(str);
                                    const result = ImageSource.fromFileSync(filePath);
                                    imageSource = result instanceof Promise ? await result : (result as unknown as ImageSource);
                                } catch (e3) {}
                            }
                        }
                    } catch (e4) {}
                }
                
                if (!imageSource) {
                    console.error("All extraction methods failed - could not extract image data from HttpContent");
                    console.error("Content type:", typeof content);
                    console.error("Content has toBase64String:", !!(content as any).toBase64String);
                    console.error("Content has raw:", !!(content as any).raw);
                }
            } catch (e) {
                console.error("Failed to extract image from HttpContent:", e);
            }
        }
        
        if (!imageSource) {
            console.error("All methods failed - could not create ImageSource");
            console.error("URL:", absoluteUrl);
            console.error("Status:", response.statusCode);
            return undefined;
        }

        const saved = imageSource.saveToFile(filePath, "jpg");
        if (!saved) {
            console.error("Failed to save image to cache");
            return undefined;
        }

        imageCache.set(absoluteUrl, filePath);

        const fileUrl = filePath.startsWith('file://') ? filePath : `file://${filePath}`;
        return fileUrl;
    } catch (error) {
        console.error("Error downloading authenticated image:", error);
        return undefined;
    }
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

    if (response.statusCode >= 400) {
        let errorMessage = `Request failed with status ${response.statusCode}`;

        try {
            const json = response.content?.toJSON() ?? {};

            if (json.message) {
                errorMessage = json.message;
            } else if (json.error) {
                errorMessage = typeof json.error === 'string' ? json.error : json.error.message || errorMessage;
            } else if (json.errorMessage) {
                errorMessage = json.errorMessage;
            } else if (typeof json === 'string') {
                errorMessage = json;
            }
        } catch (e) {
            console.error("Failed to parse error response:", e);
        }
        
        throw new Error(errorMessage);
    }

    const json = response.content?.toJSON() ?? {};

    if (!json.success) {
        throw new Error(json.message || "API Error");
    }

    return json.data;
}
