import { defineStore } from "pinia";

export type Delivery = {
  id: string;
  title: string;
  description: string;
  lat: number;
  lng: number;
};

export const useDeliveryStore = defineStore("delivery", {
  state: () => ({
    selected: null as Delivery | null,
  }),

  actions: {
    select(delivery: Delivery) {
      this.selected = delivery;
    },
    clear() {
      this.selected = null;
    },
  },
});
