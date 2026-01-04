<template>

    <ScrollView @navigatedTo="load">
        <StackLayout class="p-4">

            <StackLayout v-for="d in deliveries" :key="d.id" class="card mb-3">
                <Label :text="d.title" class="font-bold text-lg" />
                <Label :text="d.dropoffAddress" class="text-gray-500 mb-1" />
                <Label :text="`Status: ${d.status}`" />

                <Label v-if="d.agentUsername" :text="`Agent: ${d.agentUsername}`" class="mt-1" />
            </StackLayout>

            <Label v-if="deliveries.length === 0" text="No deliveries yet." class="text-gray-500 text-center mt-6" />

        </StackLayout>
    </ScrollView>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import * as DeliveryService from "../../services/deliveries.service";
import { authStore  } from "../../stores/auth.store";

const deliveries = ref<any[]>([]);
const loading = ref(false);

async function load() {
  if (!authStore.user) return;

  loading.value = true;
  try {
    deliveries.value = await DeliveryService.getMyDeliveries();
  } finally {
    loading.value = false;
  }
}

watch(
  () => authStore.user,
  (user) => {
    if (user) load();
  },
  { immediate: true }
);
</script>>