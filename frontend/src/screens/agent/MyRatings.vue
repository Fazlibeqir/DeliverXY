<template>
  <Page>
    <ActionBar title="My Ratings" />
    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <Label v-if="avg !== null" :text="`Average: ${avg?.toFixed(2)} ★`" class="text-xl font-bold mb-3" />

        <StackLayout v-for="r in ratings" :key="r.id" class="card p-3 mb-2">
          <Label :text="`★ ${r.rating}`" class="font-bold" />
          <Label v-if="r.review" :text="r.review" class="text-gray-600" />
          <Label :text="formatDate(r.createdAt)" class="text-gray-400 text-xs" />
        </StackLayout>

        <Label v-if="!loading && ratings.length === 0" text="No ratings yet." class="text-gray-500 text-center mt-6" />
        <ActivityIndicator v-if="loading" busy="true" class="mt-4" />
      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { alert } from "@nativescript/core";
import * as RatingsService from "@/services/ratings.service";
import { authStore } from "@/stores/auth.store";

const ratings = ref<any[]>([]);
const avg = ref<number | null>(null);
const loading = ref(false);

function formatDate(d: string) {
  if (!d) return "—";
  const dt = new Date(d);
  return isNaN(dt.getTime()) ? "—" : dt.toLocaleString();
}

onMounted(async () => {
  const me = authStore.user?.id;
  if (!me) return;
  try {
    loading.value = true;
    ratings.value = await RatingsService.getUserRatings(me);
    const a = await RatingsService.getUserAverage(me);
    avg.value = typeof a === "number" ? a : Number(a);
  } catch (e: any) {
    alert(e?.message || "Failed to load ratings");
  } finally {
    loading.value = false;
  }
});
</script>