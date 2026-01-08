<template>
  <GridLayout rows="*">
    <StackLayout v-if="loading" row="0" class="loading-container">
      <ActivityIndicator busy="true" />
      <Label text="Loading wallet..." class="loading-text" />
    </StackLayout>

    <ScrollView v-else row="0">
      <StackLayout class="p-4">
        <Label text="Wallet" class="section-header mb-4" />

        <StackLayout class="card-elevated p-6 mb-4" style="background-color: #000000;">
          <Label text="Available Balance" class="text-white text-sm mb-2" opacity="0.9" />
          <Label :text="formattedBalance" class="text-white text-4xl font-bold mb-1" />
          <Label :text="wallet?.currency || 'MKD'" class="text-white text-sm" opacity="0.8" />
        </StackLayout>

        <Label text="Quick Actions" class="section-subheader mb-3" />
        <GridLayout columns="*,*" class="mb-4">
          <Button col="0" text="➕ Top Up" class="btn-primary mr-2" @tap="goToTopUp" />
          <Button col="1" text="➖ Withdraw" class="btn-secondary ml-2" @tap="goToWithdraw" />
        </GridLayout>

        <Button text="📜 View Transactions" class="btn-outline" @tap="goToTransactions" />
      </StackLayout>
    </ScrollView>
  </GridLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { alert, ActivityIndicator } from "@nativescript/core";
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