import { apiRequest } from "./api";

export type DeliveryType =
    | "PACKAGE"
    | "DOCUMENTS"
    | "PASSENGER";

export type DeliveryStatus =
    | "REQUESTED"
    | "ASSIGNED"
    | "PICKED_UP"
    | "IN_TRANSIT"
    | "DELIVERED"
    | "CANCELLED";

export function getNearbyDeliveries(
    latitude: number,
    longitude: number,
    radius = 5
) {
    return apiRequest(
        "GET",
        `/api/deliveries/nearby?latitude=${latitude}&longitude=${longitude}&radius=${radius}`
    );
}

export function getActiveDelivery() {
    return apiRequest("GET", "/api/deliveries/active");
}

export function assignDelivery(id: number) {
    return apiRequest("POST", `/api/deliveries/${id}/assign`);
}

export function getAssignedDeliveries() {
    return apiRequest("GET", "/api/deliveries/assigned");
}

export function updateDeliveryStatus(id: number, status: DeliveryStatus) {
    return apiRequest(
        "PUT",
        `/api/deliveries/${id}/status?status=${status}`
    );
}

export function estimateFare(payload: {
    pickupLatitude: number;
    pickupLongitude: number;
    dropoffLatitude: number;
    dropoffLongitude: number;
    packageType: DeliveryType;
    packageWeight: number;
    promoCode?: string;
}) {
    return apiRequest("POST", "/api/deliveries/estimate-fare", payload);
}

export function createDelivery(payload: {
    title: string;
    description: string;

    packageType: DeliveryType;
    packageWeight: number;

    pickupAddress: string;
    pickupLatitude: number;
    pickupLongitude: number;
    pickupContactName: string;
    pickupContactPhone: string;

    dropoffAddress: string;
    dropoffLatitude: number;
    dropoffLongitude: number;
    dropoffContactName: string;
    dropoffContactPhone: string;

    requestedPickupTime?: string;
    city?: string;
    paymentProvider: "WALLET";
    promoCode?: string;
}) {
    return apiRequest("POST", "/api/deliveries", payload);
}

export function getMyDeliveries() {
    return apiRequest("GET", "/api/deliveries/mine");
}

export function getDeliveryById(id: number) {
    return apiRequest("GET", `/api/deliveries/${id}`);
}
