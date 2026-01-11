<template>
  <GridLayout rows="*">
    <ScrollView row="0">
      <StackLayout class="p-4">
      <!-- Header -->
      <StackLayout class="mb-4">
        <Label text="📦 My Deliveries" class="section-header mb-1" />
        <Label text="Track and manage your delivery requests" class="text-secondary text-sm" />
      </StackLayout>

      <!-- Loading State -->
      <StackLayout v-if="store.loading" class="loading-container">
        <ActivityIndicator busy="true" />
        <Label text="Loading deliveries..." class="loading-text" />
      </StackLayout>

      <!-- Delivery Cards -->
      <StackLayout
        v-for="d in store.list"
        :key="d.id"
        class="card-elevated mb-4 p-4"
        :style="getCardStyle(d.status)"
      >
        <!-- Header with Title and Status -->
        <StackLayout class="mb-3">
          <StackLayout class="mb-2" orientation="horizontal">
            <Label :text="d.title || 'Untitled Delivery'" class="font-bold text-lg" style="flex-grow: 1;" />
            <Label :text="getStatusLabel(d.status)" :class="getStatusClass(d.status)" class="status-badge" />
          </StackLayout>
          
          <StackLayout v-if="d.description" class="mb-2">
            <Label :text="d.description" class="text-sm text-secondary" />
          </StackLayout>
        </StackLayout>

        <StackLayout class="divider mb-3" />

        <!-- Delivery Address -->
        <StackLayout class="mb-3">
          <Label text="📍 Delivery Address" class="text-xs text-secondary mb-1" />
          <Label :text="d.dropoffAddress" class="text-sm font-semibold" />
        </StackLayout>

        <!-- Agent Information -->
        <StackLayout v-if="d.agentUsername" class="mb-3">
          <StackLayout class="divider mb-3" />
          <StackLayout orientation="horizontal">
            <Label text="🚚" class="mr-2" />
            <StackLayout style="flex-grow: 1;">
              <Label text="Assigned Agent" class="text-xs text-secondary mb-1" />
              <Label :text="d.agentUsername" class="text-sm font-semibold" />
            </StackLayout>
          </StackLayout>
        </StackLayout>

        <!-- Delivery ID -->
        <StackLayout class="mb-3">
          <Label :text="`Tracking: #${d.id}`" class="text-xs text-secondary" />
        </StackLayout>

        <!-- Actions -->
        <StackLayout v-if="d.status === 'DELIVERED'" class="mt-3">
          <StackLayout class="divider mb-3" />
          <Label v-if="rated[d.id]" text="✓ You rated this delivery" class="text-success text-sm text-center mb-2" />
          <Button
            v-if="!rated[d.id]"
            text="⭐ Rate Delivery"
            class="btn-primary-compact"
            @tap="rate(d.id)"
          />
        </StackLayout>
      </StackLayout>

      <!-- Empty State -->
      <StackLayout
        v-if="!store.loading && store.list.length === 0"
        class="empty-state"
      >
        <Label text="📦" class="empty-state-icon" />
        <Label text="No deliveries yet" class="empty-state-text font-bold" />
        <Label text="Create your first delivery from the home screen" class="text-secondary text-sm mt-2" />
      </StackLayout>

    </StackLayout>
    </ScrollView>
  </GridLayout>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import { ActivityIndicator } from "@nativescript/core";
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

function getCardStyle(status: string): string {
  // Add subtle left border to distinguish cards
  const borderColors: Record<string, string> = {
    PENDING: "#FFC107",
    ASSIGNED: "#17A2B8",
    PICKED_UP: "#6C757D",
    IN_TRANSIT: "#0DCAF0",
    DELIVERED: "#198754",
    CANCELLED: "#DC3545",
  };
  const color = borderColors[status] || "#CCCCCC";
  return `border-left-width: 4; border-left-color: ${color};`;
}
</script>
