import { apiRequest } from "./api";


export async function getWallet(){
    return await apiRequest("GET", "/api/wallet");
}
export async function getWalletTransactions() {
    return apiRequest("GET", "/api/wallet/transactions");
  }
  

  export async function initiateTopUp(
    amount: number,
    provider: "STRIPE" | "MOCK" = "STRIPE"
  ) {
    return apiRequest("POST", "/api/wallet/topup/initiate", {
      amount,
      provider,
    });
  }
  export async function finishTopUp(
    topUpId: number,
    success: boolean,
    referenceId?: string
  ) {
    return apiRequest(
      "POST",
      `/api/wallet/topup/callback?topUpId=${topUpId}&success=${success}&referenceId=${referenceId ?? ""}`
    );
  }
  
  export async function withdrawFromWallet(
    amount: number,
    reference?: string
  ) {
    return apiRequest("POST", "/api/wallet/withdraw", {
      amount,
      reference: reference ?? "USER_WITHDRAW",
    });
  }
  
  export async function depositToWallet(
    amount: number,
    reference?: string
  ) {
    return apiRequest("POST", "/api/wallet/deposit", {
      amount,
      reference: reference ?? "MANUAL_DEPOSIT",
    });
  }