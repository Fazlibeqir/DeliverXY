<template>
    <StackLayout class="p-6 bg-white">
      <Label text="Confirm Payment" class="section-header mb-4" />
  
      <StackLayout class="card-elevated p-4 mb-4">
        <StackLayout class="mb-3">
          <Label :text="`${props.amount.toFixed(2)} ден`" class="text-3xl font-bold text-primary mb-1" />
          <Label text="Total Amount" class="text-xs text-secondary" />
        </StackLayout>
        <StackLayout class="divider" />
        <StackLayout class="mt-3">
          <Label :text="`Distance: ${props.distanceKm.toFixed(2)} km`" class="text-sm mb-1" />
          <Label :text="`Estimated Time: ${props.etaMinutes} minutes`" class="text-sm" />
        </StackLayout>
      </StackLayout>

      <StackLayout class="card p-3 mb-4">
        <Label text="Wallet Balance" class="text-xs text-secondary mb-1" />
        <Label :text="`${wallet?.balance ?? 0} ${wallet?.currency || 'ден'}`" class="text-lg font-bold" />
      </StackLayout>
  
      <Label
        v-if="!hasEnough"
        text="⚠ Insufficient wallet balance. Please top up your wallet."
        class="text-danger text-sm mb-4 p-3"
        textWrap="true"
      />
  
      <Button
        text="✓ Pay with Wallet"
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