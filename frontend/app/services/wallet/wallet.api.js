import { apiFetch } from "../core/api-fetch";

export function getWallet() {
    return apiFetch("/wallet");
}

export function deposit(amount) {
    return apiFetch("/wallet/deposit", {
        method: "POST",
        body: JSON.stringify({ amount })
    });
}

export function withdraw(amount) {
    return apiFetch("/wallet/withdraw", {
        method: "POST",
        body: JSON.stringify({ amount })
    });
}

export function getTransactions() {
    return apiFetch("/wallet/transactions");
}
