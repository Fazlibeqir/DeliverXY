<template>
  <Page>
    <ActionBar title="Earnings & Payouts" />
    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <Label text="Summary (Last 30 Days)" class="section-header mb-3" />
        <StackLayout class="card-elevated p-4 mb-4" v-if="summary">
          <StackLayout class="mb-3">
            <Label :text="`${format(summary.totalEarned)} ден`" class="text-3xl font-bold text-primary mb-1" />
            <Label text="Total Earned" class="text-xs text-secondary" />
          </StackLayout>
          <StackLayout class="divider" />
          <GridLayout columns="*,*" class="mt-3">
            <StackLayout col="0">
              <Label :text="`${format(summary.totalTips)} ден`" class="text-lg font-bold text-success mb-1" />
              <Label text="Tips" class="text-xs text-secondary" />
            </StackLayout>
            <StackLayout col="1">
              <Label :text="`${summary.totalDeliveries}`" class="text-lg font-bold mb-1" />
              <Label text="Deliveries" class="text-xs text-secondary" />
            </StackLayout>
          </GridLayout>
          <Label :text="`${summary.totalDistanceKm} km total distance`" class="text-sm text-secondary mt-3" />
        </StackLayout>

        <Label text="Recent Earnings" class="section-header mb-3" />
        <StackLayout v-for="e in earnings" :key="e.deliveryId + '-' + e.createdAt" class="card-elevated p-3 mb-3">
          <StackLayout class="mb-2">
            <Label :text="`Delivery #${e.deliveryId}`" class="font-bold text-lg mb-1" />
            <Label :text="new Date(e.createdAt).toLocaleString()" class="text-xs text-secondary" />
          </StackLayout>
          <StackLayout class="divider" />
          <StackLayout class="mt-2">
            <Label :text="`${format(e.earnings)} ден`" class="text-xl font-bold text-primary mb-1" />
            <Label text="Base Earnings" class="text-xs text-secondary" />
            <Label v-if="e.tip" :text="`+ ${format(e.tip)} ден tip`" class="text-success text-sm mt-1" />
          </StackLayout>
        </StackLayout>

        <Button :isEnabled="!loading && page < totalPages" text="Load More" class="btn-outline mt-2" @tap="loadMore" />

        <StackLayout v-if="loading" class="loading-container">
          <ActivityIndicator busy="true" />
          <Label text="Loading..." class="loading-text" />
        </StackLayout>

        <StackLayout v-if="!loading && earnings.length === 0" class="empty-state">
          <Label text="💰" class="empty-state-icon" />
          <Label text="No earnings yet" class="empty-state-text" />
        </StackLayout>
      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { alert } from "@nativescript/core";
import * as EarningsService from "@/services/earnings.service";

const summary = ref<any>(null);
const earnings = ref<any[]>([]);
const page = ref(0);
const size = ref(10);
const totalPages = ref(1);
const loading = ref(false);

function format(x: any) {
  const n = Number(x);
  return isNaN(n) ? "0" : n.toFixed(2);
}

async function loadSummary() {
  try {
    summary.value = await EarningsService.getSummary();
  } catch {}
}

async function loadMore() {
  if (page.value >= totalPages.value) return;
  try {
    loading.value = true;
    const res = await EarningsService.getEarnings(page.value, size.value);
    const content = res?.content ?? res?.items ?? [];
    earnings.value = earnings.value.concat(content);
    totalPages.value = res?.totalPages ?? totalPages.value;
    page.value = (res?.number ?? page.value) + 1;
  } catch {
    alert("Failed to load earnings");
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadSummary();
  await loadMore();
});
</script>