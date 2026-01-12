import { Http } from "@nativescript/core";
import { secureStorage, TOKEN_KEYS } from "./secure-storage";
import { ImageSource } from "@nativescript/core";
import { API_URL } from "../config";

async function authHeaders() {
    const token = await secureStorage.get({ key: TOKEN_KEYS.access });
    return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function uploadKYCFile(
    filePath: string,
    documentType: "ID_FRONT" | "ID_BACK" | "SELFIE" | "PROOF_OF_ADDRESS"
) {
    const token = await secureStorage.get({ key: TOKEN_KEYS.access });

    const image = ImageSource.fromFileSync(filePath);
    if (!image) throw new Error("Failed to load image");

    const base64 = image.toBase64String("jpg", 85);
    const res = await Http.request({
        url: `${API_URL}/api/upload/kyc/base64`,
        method: "POST",
        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
        },
        content: JSON.stringify({
            documentType,
            base64,
        }),
    });

    const json = res.content?.toJSON();
    if (!json?.success) throw new Error(json?.message || "Upload failed");

    return json.data;
}

export async function getMyKYC() {
    const headers = { ... (await authHeaders()), "Content-Type": "application/json" };

    const res = await Http.request({
        url: `${API_URL}/api/kyc`,
        method: "GET",
        headers,
    });

    const json = res.content?.toJSON();
    if (!json?.success) throw new Error(json?.message || "Failed to load KYC");

    return json.data;
}

export async function submitKYC(payload: {
    idFrontUrl?: string;
    idBackUrl?: string;
    selfieUrl?: string;
    proofOfAddressUrl?: string;
}) {
    const headers = {
        ...(await authHeaders()),
        "Content-Type": "application/json",
    };

    const res = await Http.request({
        url: `${API_URL}/api/kyc`,
        method: "PUT",
        headers,
        content: JSON.stringify(payload),
    });

    const json = res.content?.toJSON();
    if (!json?.success) throw new Error(json?.message || "Submit failed");

    return json.data;
}
