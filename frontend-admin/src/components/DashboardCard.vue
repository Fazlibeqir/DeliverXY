<template>
  <div class="card">
    <h2 class="text-sm font-medium text-neutral-400 mb-2">{{ title }}</h2>
    <p class="text-4xl font-bold text-white">{{ count }}</p>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  count: [Number, String],
  color: String,
})
</script>
