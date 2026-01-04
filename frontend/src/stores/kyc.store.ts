import { defineStore } from "pinia";
import * as KYCService from "../services/kyc.service";

export type KYCStatus = "PENDING" | "APPROVED" | "REJECTED";

export const useKYCStore = defineStore("kyc", {
  state: () => ({
    status: null as KYCStatus | null,
    loading: false,
    idFrontUrl: "",
    idBackUrl: "",
    selfieUrl: "",
    proofOfAddressUrl: "",
    rejectionReason: "",
  }),

  actions: {
    async load() {
      this.loading = true;
      try {
        const kyc = await KYCService.getMyKYC();
        if (!kyc) return;

        this.status = kyc.status;
        this.idFrontUrl = kyc.idFrontUrl;
        this.idBackUrl = kyc.idBackUrl;
        this.selfieUrl = kyc.selfieUrl;
        this.proofOfAddressUrl = kyc.proofOfAddressUrl;
        this.rejectionReason = kyc.rejectionReason;
      } finally {
        this.loading = false;
      }
    },

    async submit() {
      this.loading = true;
      try {
        const res = await KYCService.submitKYC({
          idFrontUrl: this.idFrontUrl,
          idBackUrl: this.idBackUrl,
          selfieUrl: this.selfieUrl,
          proofOfAddressUrl: this.proofOfAddressUrl,
        });
        this.status = res.status;
      } finally {
        this.loading = false;
      }
    },
  },
});
