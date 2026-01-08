<template>
  <GridLayout rows="auto,*">
    <ScrollView row="1">
      <StackLayout class="p-4">
        <Label v-if="store.loading" text="Loading deliveries..." class="loading-text text-center" />

        <StackLayout v-for="d in store.assigned" :key="d.id" class="card-elevated mb-4">
          <StackLayout class="mb-3">
            <Label :text="d.title" class="font-bold text-lg mb-2" />
            <StackLayout class="mb-2">
              <Label text="Pickup" class="text-xs text-secondary mb-1" />
              <Label :text="d.pickupAddress" class="text-sm" />
            </StackLayout>
            <StackLayout v-if="d.dropoffAddress" class="mb-2">
              <Label text="Dropoff" class="text-xs text-secondary mb-1" />
              <Label :text="d.dropoffAddress" class="text-sm" />
            </StackLayout>
            <Label :text="getStatusLabel(d.status)" :class="getStatusClass(d.status)" class="status-badge" />
          </StackLayout>

          <StackLayout class="divider" />

          <StackLayout class="mt-3">
            <Button v-if="d.status === 'ASSIGNED'" text="✓ Mark as Picked Up" class="btn-success mb-2" 
              @tap="store.updateStatus(d.id, 'PICKED_UP')" />

            <Button v-if="d.status === 'PICKED_UP'" text="🚚 Start Delivery" class="btn-primary mb-2" 
              @tap="store.updateStatus(d.id, 'IN_TRANSIT')" />

            <Button v-if="d.status === 'IN_TRANSIT'" text="✓ Mark as Delivered" class="btn-success mb-2" 
              @tap="store.updateStatus(d.id, 'DELIVERED')" />

            <Button v-if="canCancel(d.status)" text="Cancel Delivery" class="btn-danger"
              @tap="store.updateStatus(d.id, 'CANCELLED')" />
          </StackLayout>
        </StackLayout>

        <StackLayout v-if="!store.loading && store.assigned.length === 0" class="empty-state">
          <Label text="📦" class="empty-state-icon" />
          <Label text="No assigned deliveries" class="empty-state-text" />
          <Label text="Check the map for available deliveries" class="text-secondary text-sm mt-2" />
        </StackLayout>

      </StackLayout>
    </ScrollView>
  </GridLayout>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from "vue";
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
</script>
