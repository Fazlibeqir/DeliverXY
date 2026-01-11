import { defineStore } from "pinia";
import * as DeliveryService from "../services/deliveries.service";
import type { DeliveryStatus } from "../services/deliveries.service";

export type Delivery = {
  id: number;
  title: string;
  pickupAddress: string;
  status: DeliveryStatus;
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
        const deliveries = await DeliveryService.getAssignedDeliveries();
        // Sort by createdAt descending (newest first)
        this.assigned = deliveries.sort((a: any, b: any) => {
          const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
          const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
          return dateB - dateA; // Descending order
        });
      } finally {
        this.loading = false;
      }
    },

    async updateStatus(id: number, status: DeliveryStatus) {
      const updated = await DeliveryService.updateDeliveryStatus(id, status);
      await this.loadAssigned();
      (globalThis as any).__deliveryStatusChanged?.(updated);
    },

    async refreshDeliveries() {
      await this.loadAssigned();
    },

    remove(id: number) {
      this.assigned = this.assigned.filter(d => d.id !== id);
    },
  },
});

export const useClientDeliveriesStore = defineStore("clientDeliveries", {
  state: () => ({
    list: [] as any[],
    loading: false,
  }),

  actions: {
    async loadMine(force = false) {
      if (this.loading && !force) return;

      this.loading = true;
      try {
        const deliveries = await DeliveryService.getMyDeliveries();
        // Sort by createdAt descending (newest first)
        this.list = deliveries.sort((a: any, b: any) => {
          const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
          const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
          return dateB - dateA; // Descending order
        });
      } finally {
        this.loading = false;
      }
    },
  },
});
