<template>
    <StackLayout class="p-4 bg-white">
      <Label text="Confirm payment" class="text-xl font-bold mb-2" />
  
      <Label
        :text="`Price: ${props.amount.toFixed(2)} ден`"
        class="text-lg font-bold mb-1"
      />
  
      <Label
        :text="`Distance: ${props.distanceKm.toFixed(2)} km`"
        class="text-gray-600 mb-1"
      />
  
      <Label
        :text="`ETA: ${props.etaMinutes} min`"
        class="text-gray-600 mb-3"
      />
  
      <Label
        :text="`Wallet balance: ${wallet?.balance ?? 0} ден`"
        class="mb-4"
      />
  
      <Label
        v-if="!hasEnough"
        text="Insufficient wallet balance"
        class="text-red-600 mb-3"
      />
  
      <Button
        text="Pay with wallet"
        class="btn-primary mb-2"
        :isEnabled="hasEnough && !loading"
        @tap="confirm"
      />
  
      <Button
        text="Cancel"
        class="btn-secondary"
        @tap="$closeModal(false)"
      />
    </StackLayout>
  </template>
  

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { $closeModal } from "nativescript-vue";
import * as WalletService from "@/services/wallet.service";

const props = defineProps<{
    amount: number;
    distanceKm: number;
    etaMinutes: number;
}>();

const wallet = ref<any>(null);
const loading = ref(false);

const hasEnough = computed(() =>
    wallet.value && Number(wallet.value.balance) >= props.amount
);

onMounted(async () => {
    wallet.value = await WalletService.getWallet();
});

async function confirm() {
    loading.value = true;
    try {
        $closeModal(true);
    } finally {
        loading.value = false;
    }
}
</script>