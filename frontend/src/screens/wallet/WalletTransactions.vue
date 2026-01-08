<template>
  <Page>
    <ActionBar title="Transactions" />

    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <Label v-if="transactions.length === 0" text="No transactions yet" class="text-gray-500" />

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

onMounted(async () => {
  try {
    transactions.value = await getWalletTransactions();
  } catch (e) {
    alert("Failed to load transactions");
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