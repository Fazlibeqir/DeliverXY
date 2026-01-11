<template>
    <div :class="`bg-${color}-100 p-6 rounded-xl shadow hover:shadow-lg transition`">
      <h2 class="text-xl font-semibold text-gray-700 mb-2">{{ title }}</h2>
      <p class="text-4xl font-bold text-gray-900">{{ count }}</p>
    </div>
  </template>
  
  <script setup>
  defineProps({
    title: String,
    count: Number,
    color: String
  })
  </script>
  