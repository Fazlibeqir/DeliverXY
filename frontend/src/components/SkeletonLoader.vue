<template>
  <StackLayout :class="containerClass" :style="containerStyle">
    <!-- Map Skeleton -->
    <GridLayout v-if="type === 'map'" rows="*" class="skeleton-map">
      <StackLayout row="0" class="skeleton-shimmer" />
    </GridLayout>
    
    <!-- List Item Skeleton -->
    <StackLayout v-else-if="type === 'list-item'" class="skeleton-list-item">
      <GridLayout columns="auto, *" columnsGap="12" class="mb-3">
        <!-- Avatar/Icon -->
        <StackLayout 
          col="0" 
          class="skeleton-circle skeleton-shimmer"
          :style="{ width: size === 'small' ? 40 : 50, height: size === 'small' ? 40 : 50, borderRadius: size === 'small' ? 20 : 25 }"
        />
        <!-- Content -->
        <StackLayout col="1">
          <StackLayout 
            class="skeleton-line skeleton-shimmer mb-2"
            :style="{ height: size === 'small' ? 16 : 20, width: '80%' }"
          />
          <StackLayout 
            class="skeleton-line skeleton-shimmer"
            :style="{ height: size === 'small' ? 12 : 14, width: '60%' }"
          />
        </StackLayout>
      </GridLayout>
    </StackLayout>
    
    <!-- Card Skeleton -->
    <StackLayout v-else-if="type === 'card'" class="skeleton-card skeleton-shimmer">
      <StackLayout 
        class="skeleton-line skeleton-shimmer mb-3"
        :style="{ height: 20, width: '70%' }"
      />
      <StackLayout 
        class="skeleton-line skeleton-shimmer mb-2"
        :style="{ height: 14, width: '100%' }"
      />
      <StackLayout 
        class="skeleton-line skeleton-shimmer mb-2"
        :style="{ height: 14, width: '90%' }"
      />
      <StackLayout 
        class="skeleton-line skeleton-shimmer"
        :style="{ height: 14, width: '50%' }"
      />
    </StackLayout>
    
    <!-- Delivery Panel Skeleton -->
    <StackLayout v-else-if="type === 'delivery-panel'" class="skeleton-delivery-panel">
      <StackLayout 
        class="skeleton-line skeleton-shimmer mb-3"
        :style="{ height: 24, width: '60%' }"
      />
      <StackLayout 
        class="skeleton-line skeleton-shimmer mb-4"
        :style="{ height: 16, width: '100%' }"
      />
      <GridLayout columns="*, *" columnsGap="8">
        <StackLayout 
          col="0"
          class="skeleton-button skeleton-shimmer"
          :style="{ height: 44, borderRadius: 12 }"
        />
        <StackLayout 
          col="1"
          class="skeleton-button skeleton-shimmer"
          :style="{ height: 44, borderRadius: 12 }"
        />
      </GridLayout>
    </StackLayout>
    
    <!-- Multiple List Items -->
    <StackLayout v-else-if="type === 'list'" class="skeleton-list">
      <SkeletonLoader 
        v-for="i in count" 
        :key="i" 
        type="list-item" 
        :size="size"
      />
    </StackLayout>
    
    <!-- Custom Content -->
    <StackLayout v-else class="skeleton-custom">
      <slot />
    </StackLayout>
  </StackLayout>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(defineProps<{
  type?: 'map' | 'list-item' | 'card' | 'delivery-panel' | 'list' | 'custom';
  count?: number;
  size?: 'small' | 'medium' | 'large';
  containerClass?: string;
  containerStyle?: string;
}>(), {
  type: 'custom',
  count: 3,
  size: 'medium',
  containerClass: '',
  containerStyle: ''
});
</script>

<style scoped>
.skeleton-shimmer {
  background: linear-gradient(
    90deg,
    #f0f0f0 0%,
    #e0e0e0 50%,
    #f0f0f0 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.skeleton-map {
  width: 100%;
  height: 100%;
  background-color: #f5f5f5;
}

.skeleton-list-item {
  padding: 12px;
  background-color: #ffffff;
  border-radius: 8;
}

.skeleton-card {
  padding: 16px;
  background-color: #ffffff;
  border-radius: 12;
  margin-bottom: 12px;
}

.skeleton-delivery-panel {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 16 16 0 0;
}

.skeleton-line {
  background-color: #e0e0e0;
  border-radius: 4;
}

.skeleton-circle {
  background-color: #e0e0e0;
}

.skeleton-button {
  background-color: #e0e0e0;
}

.skeleton-list {
  padding: 16px;
}
</style>
