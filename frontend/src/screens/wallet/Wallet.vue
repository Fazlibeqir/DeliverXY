<template>
  <!-- ONE stable root -->
  <StackLayout>

    <!-- Loading state -->
    <Label
      v-if="loading"
      text="Loading wallet..."
      class="text-gray-400 p-4"
    />

    <!-- Content -->
    <StackLayout v-else class="p-4">
      <Label text="Wallet" class="text-2xl font-bold mb-4" />

      <StackLayout class="card p-4 mb-4">
        <Label text="Balance" class="text-gray-500 text-sm" />
        <Label :text="formattedBalance" class="text-3xl font-bold mt-1" />
      </StackLayout>

      <Button text="➕ Top up" class="btn-primary mb-2" @tap="goToTopUp" />
      <Button text="➖ Withdraw" class="btn-secondary mb-2" @tap="goToWithdraw" />
      <Button text="📜 Transactions" class="btn-outline" @tap="goToTransactions" />
    </StackLayout>

  </StackLayout>
</template>


<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { alert } from "@nativescript/core";
import { $navigateTo } from "nativescript-vue";

import { getWallet } from "@/services/wallet.service";
import WalletTopUp from "./WalletTopUp.vue";
import WalletWithdraw from "./WalletWithdraw.vue";
import WalletTransactions from "./WalletTransactions.vue";

const wallet = ref<any>(null);
const loading = ref(true);

async function loadWallet() {
  try {
      
      loading.value = true;
      wallet.value = await getWallet();
  } catch {
      alert("Failed to load wallet");
  } finally {
      loading.value = false;
  }
}

onMounted(loadWallet);
(globalThis as any).__refreshWallet = loadWallet;

const formattedBalance = computed(() => {
  if (!wallet.value) return "—";
  return `${wallet.value.balance} ${wallet.value.currency}`;
});

function goToTopUp() {
  $navigateTo(WalletTopUp);
}

function goToTransactions() {
  $navigateTo(WalletTransactions);
}

function goToWithdraw() {
$navigateTo(WalletWithdraw);
}
</script>