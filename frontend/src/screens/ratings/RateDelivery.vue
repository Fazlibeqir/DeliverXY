<template>
  <Page>
    <ActionBar title="Rate Delivery" />

    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <StackLayout class="card-elevated p-4 mb-4">
          <Label text="Delivery Information" class="section-subheader mb-2" />
          <Label :text="`Delivery #${deliveryId}`" class="font-bold" />
        </StackLayout>

        <StackLayout class="mb-4">
          <Label text="Rating" class="section-subheader mb-2" />
          <Label text="How would you rate this delivery?" class="text-sm text-secondary mb-3" />
          <ListPicker :items="ratingLabels" v-model="ratingIndex" class="mb-2" />
        </StackLayout>

        <StackLayout class="mb-4">
          <Label text="Comment (Optional)" class="section-subheader mb-2" />
          <Label text="Share your experience" class="text-sm text-secondary mb-2" />
          <TextView v-model="review" hint="Write your review here..." class="input" />
        </StackLayout>

        <Button :isEnabled="!loading" text="Submit Rating" class="btn-primary" @tap="submit" />

        <StackLayout v-if="loading" class="loading-container">
          <ActivityIndicator busy="true" />
          <Label text="Submitting..." class="loading-text" />
        </StackLayout>
      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { alert, Frame } from "@nativescript/core";
import * as RatingsService from "@/services/ratings.service";

const props = defineProps<{ deliveryId: number }>();

const ratingOptions = [1, 2, 3, 4, 5];
const ratingLabels = ["1 ★", "2 ★★", "3 ★★★", "4 ★★★★", "5 ★★★★★"];
const ratingIndex = ref(4); // default 5
const review = ref("");
const loading = ref(false);

async function submit() {
  try {
    loading.value = true;
    await RatingsService.createRating({
      deliveryId: props.deliveryId,
      rating: ratingOptions[ratingIndex.value],
      review: review.value?.trim() || undefined,
    });
    (globalThis as any).__deliveryRated?.(props.deliveryId);
    await alert("Thank you for your feedback!");
    Frame.topmost().goBack();
  } catch (e: any) {
    await alert(e?.message || "Failed to submit rating");
  } finally {
    loading.value = false;
  }
}
</script>