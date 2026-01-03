import { defineStore } from "pinia";
import * as DeliveryService from "../services/deliveries.service";

export type Delivery = {
  id: number;
  title: string;
  pickupAddress: string;
  status: string;
};

export const useDeliveriesStore = defineStore("deliveries", {
  state: () => ({
    assigned: [] as Delivery[],
    loading: false,
  }),

  actions: {
    async loadAssigned(force = false) {
      if (this.loading && !force) return;
      
      this.loading = true;
      try {
        this.assigned = await DeliveryService.getAssignedDeliveries();
      } finally {
        this.loading = false;
      }
    },

    async updateStatus(id: number, status: string) {
      await DeliveryService.updateDeliveryStatus(id, status);
      await this.loadAssigned();
    },

    async refreshDeliveries() {
      await this.loadAssigned();
    },

    remove(id: number) {
      this.assigned = this.assigned.filter(d => d.id !== id);
    },
  },
});
