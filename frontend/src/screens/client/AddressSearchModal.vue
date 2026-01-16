<template>
  <GridLayout rows="auto, *, auto" class="bg-white">
    <!-- Header -->
    <GridLayout row="0" columns="*, auto" class="p-4" style="border-bottom-width: 1; border-bottom-color: #e0e0e0;">
      <StackLayout col="0">
        <Label text="🔍 Search Address" class="text-lg font-bold" />
        <Label text="Type address or place name" class="text-xs text-secondary mt-1" />
      </StackLayout>
      <Button col="1" text="✕" class="btn-outline" 
              style="width: 40; height: 40; font-size: 18; padding: 0;"
              @tap="close" />
    </GridLayout>

    <!-- Search Input -->
    <StackLayout row="1" class="p-4">
      <TextField v-model="query" hint="📍 Type address, place name, or landmark" 
                 class="input mb-3" @textChange="onSearch" 
                 returnKeyType="search" />
      
      <ActivityIndicator v-if="searching" busy="true" class="mb-3" />
      
      <ScrollView v-if="results.length > 0" style="max-height: 400;">
        <StackLayout>
          <StackLayout 
            v-for="(result, index) in results" 
            :key="result.label" 
            class="p-3"
            :style="index < results.length - 1 ? 'border-bottom-width: 1; border-bottom-color: #e0e0e0;' : ''"
            @tap="selectResult(result)" 
          >
            <Label :text="result.label" class="text-sm" textWrap="true" />
            <Label :text="`${result.lat.toFixed(6)}, ${result.lng.toFixed(6)}`" 
                   class="text-xs text-secondary mt-1" />
          </StackLayout>
        </StackLayout>
      </ScrollView>
      
      <Label v-if="query.length >= 3 && results.length === 0 && !searching" 
             text="No results found. Try a different search term." 
             class="text-center text-secondary p-4" 
             textWrap="true" />
      
      <Label v-if="query.length < 3 && !searching" 
             text="Type at least 3 characters to search" 
             class="text-center text-secondary p-4" />
    </StackLayout>

    <!-- Footer -->
    <StackLayout row="2" class="p-4" style="border-top-width: 1; border-top-color: #e0e0e0;">
      <Button text="Cancel" class="btn-outline" @tap="close" />
    </StackLayout>
  </GridLayout>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from "vue";
import { $closeModal } from "nativescript-vue";
import { searchAddress } from "@/services/geocoding.service";
import { debounce } from "@/utils/debounce";

const query = ref("");
const results = ref<{ label: string; lat: number; lng: number }[]>([]);
const searching = ref(false);

async function performSearch() {
  if (query.value.length < 3) {
    results.value = [];
    searching.value = false;
    return;
  }

  searching.value = true;
  try {
    results.value = await searchAddress(query.value);
  } catch (error) {
    results.value = [];
  } finally {
    searching.value = false;
  }
}

// Debounce search with 400ms delay
const onSearch = debounce(performSearch, 400);

onUnmounted(() => {
  // Cleanup is handled by debounce utility
});

function selectResult(result: { label: string; lat: number; lng: number }) {
  $closeModal(result);
}

function close() {
  $closeModal(null);
}
</script>
