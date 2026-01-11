<template>
  <GridLayout rows="auto,*">
    <ScrollView row="1">
      <StackLayout class="p-4">
        <!-- Header -->
        <StackLayout class="mb-4">
          <Label text="🚚 My Deliveries" class="section-header mb-1" />
          <Label text="Manage your assigned deliveries" class="text-secondary text-sm" />
        </StackLayout>

        <!-- Loading State -->
        <StackLayout v-if="store.loading" class="loading-container">
          <ActivityIndicator busy="true" />
          <Label text="Loading deliveries..." class="loading-text" />
        </StackLayout>

        <!-- Delivery Cards -->
        <StackLayout 
          v-for="d in store.assigned" 
          :key="d.id" 
          class="card-elevated mb-4 p-4"
          :style="getCardStyle(d.status)"
        >
          <!-- Header -->
          <StackLayout class="mb-3">
            <StackLayout class="mb-2" orientation="horizontal">
              <Label :text="d.title || 'Untitled Delivery'" class="font-bold text-lg" style="flex-grow: 1;" />
              <Label :text="getStatusLabel(d.status)" :class="getStatusClass(d.status)" class="status-badge" />
            </StackLayout>
          </StackLayout>

          <StackLayout class="divider mb-3" />

          <!-- Locations -->
          <StackLayout class="mb-3">
            <StackLayout class="mb-3">
              <Label text="📍 Pickup Location" class="text-xs text-secondary mb-1" />
              <Label :text="d.pickupAddress" class="text-sm font-semibold" />
            </StackLayout>
            <StackLayout v-if="d.dropoffAddress" class="mb-2">
              <Label text="🎯 Dropoff Location" class="text-xs text-secondary mb-1" />
              <Label :text="d.dropoffAddress" class="text-sm font-semibold" />
            </StackLayout>
          </StackLayout>

          <StackLayout class="divider mb-3" />

          <!-- Delivery ID -->
          <StackLayout class="mb-3">
            <Label :text="`Tracking: #${d.id}`" class="text-xs text-secondary" />
          </StackLayout>

          <!-- Action Buttons -->
          <StackLayout class="mt-3">
            <Button 
              v-if="d.status === 'ASSIGNED'" 
              text="✓ Mark as Picked Up" 
              class="btn-success mb-2" 
              @tap="store.updateStatus(d.id, 'PICKED_UP')" 
            />

            <Button 
              v-if="d.status === 'PICKED_UP'" 
              text="🚚 Start Delivery" 
              class="btn-primary mb-2" 
              @tap="store.updateStatus(d.id, 'IN_TRANSIT')" 
            />

            <Button 
              v-if="d.status === 'IN_TRANSIT'" 
              text="✓ Mark as Delivered" 
              class="btn-success mb-2" 
              @tap="store.updateStatus(d.id, 'DELIVERED')" 
            />

            <Button 
              v-if="canCancel(d.status)" 
              text="❌ Cancel Delivery" 
              class="btn-outline"
              @tap="store.updateStatus(d.id, 'CANCELLED')" 
            />
          </StackLayout>
        </StackLayout>

        <!-- Empty State -->
        <StackLayout v-if="!store.loading && store.assigned.length === 0" class="empty-state">
          <Label text="📦" class="empty-state-icon" />
          <Label text="No assigned deliveries" class="empty-state-text font-bold" />
          <Label text="Check the map on the Home tab for available deliveries" class="text-secondary text-sm mt-2" />
        </StackLayout>

      </StackLayout>
    </ScrollView>
  </GridLayout>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from "vue";
import { ActivityIndicator } from "@nativescript/core";
import { useDeliveriesStore } from "../stores/useDeliveryStore";
import { authStore } from "../stores/auth.store";
import type { DeliveryStatus } from "../services/deliveries.service";

const store = useDeliveriesStore();

async function loadAssigned() {
  await store.loadAssigned();
}

watch(
  () => authStore.user,
  (user) => {
    if (user) store.loadAssigned(true);
  },
  { immediate: true }
);
onMounted(loadAssigned);

(globalThis as any).__agentDeliveriesTabActivated = loadAssigned;

onUnmounted(() => {
  delete (globalThis as any).__agentDeliveriesTabActivated;
});

function canCancel(status: DeliveryStatus) {
  return status === "ASSIGNED"
    || status === "PICKED_UP"
    || status === "IN_TRANSIT";
}

function getStatusLabel(status: DeliveryStatus): string {
  const labels: Record<DeliveryStatus, string> = {
    PENDING: "Pending",
    ASSIGNED: "Assigned",
    PICKED_UP: "Picked Up",
    IN_TRANSIT: "In Transit",
    DELIVERED: "Delivered",
    CANCELLED: "Cancelled",
  };
  return labels[status] || status;
}

function getStatusClass(status: DeliveryStatus): string {
  const classes: Record<DeliveryStatus, string> = {
    PENDING: "status-pending",
    ASSIGNED: "status-assigned",
    PICKED_UP: "status-picked-up",
    IN_TRANSIT: "status-in-transit",
    DELIVERED: "status-delivered",
    CANCELLED: "status-cancelled",
  };
  return classes[status] || "";
}

function getCardStyle(status: DeliveryStatus): string {
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
