import { apiFetch } from "../core/api-fetch";

export function validatePromo(orderAmount, promoCode) {
    return apiFetch("/promo-codes/validate", {
        method: "POST",
        body: JSON.stringify({ promoCode, orderAmount })
    });
}

export function getActivePromoCodes() {
    return apiFetch("/promo-codes/active");
}
