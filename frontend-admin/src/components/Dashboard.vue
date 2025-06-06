<script setup>
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="p-4">
    <h2 class="text-2xl">Welcome, {{ auth.user?.name || 'Admin' }}</h2>
  </div>
</template>
