<template>
  <ScrollView>
    <StackLayout class="p-4">

      <StackLayout
        v-for="d in store.list"
        :key="d.id"
        class="card mb-3"
      >
        <Label :text="d.title" class="font-bold text-lg" />
        <Label :text="d.dropoffAddress" class="text-gray-500 mb-1" />
        <Label :text="`Status: ${d.status}`" />

        <Label
          v-if="d.agentUsername"
          :text="`Agent: ${d.agentUsername}`"
          class="mt-1"
        />
      </StackLayout>

      <Label
        v-if="!store.loading && store.list.length === 0"
        text="No deliveries yet."
        class="text-gray-500 text-center mt-6"
      />

    </StackLayout>
  </ScrollView>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from "vue";
import { useClientDeliveriesStore } from "../../stores/useDeliveryStore";

const store = useClientDeliveriesStore();

onMounted(() => {
  (globalThis as any).__clientDeliveriesTabActivated = () => {
    store.loadMine(true);
  };
});

onUnmounted(() => {
  delete (globalThis as any).__clientDeliveriesTabActivated;
});
</script>
