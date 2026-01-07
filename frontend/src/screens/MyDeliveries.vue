<template>
  <GridLayout rows="auto,*">
    <ScrollView row="1">
      <StackLayout class="p-4">

        <Label v-if="store.loading" text="Loading..." class="text-center" />

        <StackLayout v-for="d in store.assigned" :key="d.id" class="card mb-4">
          <Label :text="d.title" class="font-bold text-lg" />
          <Label :text="d.pickupAddress" class="text-gray-500" />
          <Label :text="`Status: ${d.status}`" />

          <Button v-if="d.status === 'ASSIGNED'" text="Picked up" @tap="store.updateStatus(d.id, 'PICKED_UP')" />

          <Button v-if="d.status === 'PICKED_UP'" text="Start delivery" @tap="store.updateStatus(d.id, 'IN_TRANSIT')" />

          <Button v-if="d.status === 'IN_TRANSIT'" text="Mark delivered" @tap="store.updateStatus(d.id, 'DELIVERED')" />

          <Button v-if="canCancel(d.status)" text="Cancel" class="btn-danger"
            @tap="store.updateStatus(d.id, 'CANCELLED')" />
        </StackLayout>

        <Label v-if="!store.loading && store.assigned.length === 0" text="No assigned deliveries."
          class="text-gray-500 text-center mt-6" />

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
</script>
