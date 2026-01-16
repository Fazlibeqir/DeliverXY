<template>
  <Page>
    <ActionBar title="Transactions" />

    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <!-- Empty State -->
        <StackLayout
          v-if="!loading && transactions.length === 0"
          class="empty-state"
          style="padding: 40 20;"
        >
          <Label text="💳" class="empty-state-icon" style="font-size: 64; margin-bottom: 16;" />
          <Label text="No transactions yet" class="empty-state-text font-bold" style="font-size: 18; margin-bottom: 8;" />
          <Label text="Your transaction history will appear here" class="text-secondary text-sm text-center" />
        </StackLayout>

        <StackLayout v-for="tx in transactions" :key="tx.id" class="card p-3 mb-2">
          <Label :text="tx.type" class="font-bold" />

          <Label :text="formatAmount(tx.amount)" :class="tx.amount >= 0 ? 'text-green-600' : 'text-red-600'" />

          <Label :text="tx.reference || '—'" class="text-gray-500 text-xs" />

          <Label :text="formatDate(tx.createdAt)" class="text-gray-400 text-xs" />
        </StackLayout>
      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { alert } from "@nativescript/core";
import { getWalletTransactions } from "@/services/wallet.service";

const transactions = ref<any[]>([]);
const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    transactions.value = await getWalletTransactions();
  } catch (e) {
    alert("Failed to load transactions");
  } finally {
    loading.value = false;
  }
  (globalThis as any).__refreshWallet?.();
});

function formatAmount(amount: number) {
  return `${amount > 0 ? "+" : ""}${amount}`;
}

function formatDate(date: string) {
  if (!date) return "—";
  const d = new Date(date);
  if (isNaN(d.getTime())) return "—";
  return d.toLocaleString();
}
</script>