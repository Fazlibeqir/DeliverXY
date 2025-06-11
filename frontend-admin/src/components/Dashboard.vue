<script setup>
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="p-4">
    <span v-if="auth.token && auth.user" class="ml-auto text-gray-700">
  Hello, {{ auth.user.username }}
</span>

  </div>
</template>
