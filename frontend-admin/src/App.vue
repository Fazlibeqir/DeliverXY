<script setup>
import { RouterView, RouterLink } from 'vue-router'
</script>

<template>
  <div class="p-4">
    <nav class="mb-4">
      <RouterLink to="/" class="mr-4">Dashboard</RouterLink>
      <RouterLink to="/users">Users</RouterLink>
    </nav>
    <RouterView />
  </div>
</template>
