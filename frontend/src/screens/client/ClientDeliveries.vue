<template>
  <GridLayout rows="*">
    <ScrollView row="0">
      <StackLayout class="p-4">
      <Label v-if="store.loading" text="Loading deliveries..." class="loading-text text-center" />

      <StackLayout
        v-for="d in store.list"
        :key="d.id"
        class="card-elevated mb-4"
      >
        <StackLayout class="mb-3">
          <Label :text="d.title" class="font-bold text-lg mb-2" />
          <StackLayout class="mb-2">
            <Label text="Delivery Address" class="text-xs text-secondary mb-1" />
            <Label :text="d.dropoffAddress" class="text-sm" />
          </StackLayout>
          <Label :text="getStatusLabel(d.status)" :class="getStatusClass(d.status)" class="status-badge" />
        </StackLayout>

        <StackLayout v-if="d.agentUsername" class="mb-3">
          <StackLayout class="divider" />
          <Label text="Assigned Agent" class="text-xs text-secondary mb-1 mt-2" />
          <Label :text="d.agentUsername" class="text-sm font-semibold" />
        </StackLayout>

        <StackLayout v-if="d.status === 'DELIVERED'" class="mt-3">
          <StackLayout class="divider" />
          <Label v-if="rated[d.id]" text="✓ You rated this delivery" class="text-success text-sm mt-3 text-center" />
          <Button
            v-if="!rated[d.id]"
            text="⭐ Rate Delivery"
            class="btn-outline mt-3"
            @tap="rate(d.id)"
          />
        </StackLayout>
      </StackLayout>

      <StackLayout
        v-if="!store.loading && store.list.length === 0"
        class="empty-state"
      >
        <Label text="📦" class="empty-state-icon" />
        <Label text="No deliveries yet" class="empty-state-text" />
        <Label text="Create your first delivery from the home screen" class="text-secondary text-sm mt-2" />
      </StackLayout>

    </StackLayout>
    </ScrollView>
  </GridLayout>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import { useClientDeliveriesStore } from "../../stores/useDeliveryStore";
import RateDelivery from "../ratings/RateDelivery.vue";
import { $navigateTo } from "nativescript-vue";
import * as RatingsService from "@/services/ratings.service";
import { authStore } from "@/stores/auth.store";

const store = useClientDeliveriesStore();
const rated = ref<Record<number, boolean>>({});

onMounted(() => {
  (globalThis as any).__clientDeliveriesTabActivated = () => {
    store.loadMine(true);
    loadRatedFlags();
  };
  (globalThis as any).__deliveryRated = (deliveryId: number) => {
    rated.value[deliveryId] = true;
  };
  loadRatedFlags();
});

onUnmounted(() => {
  delete (globalThis as any).__clientDeliveriesTabActivated;
  delete (globalThis as any).__deliveryRated;
});

function rate(deliveryId: number) {
  $navigateTo(RateDelivery, { props: { deliveryId } });
}

async function loadRatedFlags() {
  const me = authStore.user?.id;
  if (!me) return;
  const delivered = store.list.filter((d: any) => d.status === "DELIVERED");
  for (const d of delivered) {
    if (rated.value[d.id]) continue;
    try {
      const list = await RatingsService.getDeliveryRatings(d.id);
      const mine = (list || []).some((r: any) => {
        const rid = r?.reviewer?.id ?? r?.reviewerId;
        return rid === me;
      });
      if (mine) rated.value[d.id] = true;
    } catch {}
  }
}

function getStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    PENDING: "Pending",
    ASSIGNED: "Assigned",
    PICKED_UP: "Picked Up",
    IN_TRANSIT: "In Transit",
    DELIVERED: "Delivered",
    CANCELLED: "Cancelled",
  };
  return labels[status] || status;
}

function getStatusClass(status: string): string {
  const classes: Record<string, string> = {
    PENDING: "status-pending",
    ASSIGNED: "status-assigned",
    PICKED_UP: "status-picked-up",
    IN_TRANSIT: "status-in-transit",
    DELIVERED: "status-delivered",
    CANCELLED: "status-cancelled",
  };
  return classes[status] || "";
}
</script>
