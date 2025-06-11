<script setup>
import { RouterView, RouterLink } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="p-4">
    <nav class="mb-4 flex items-center gap-4">
      <RouterLink to="/">Dashboard</RouterLink>
      <RouterLink to="/users">Users</RouterLink>
      <span v-if="auth.token" class="ml-auto">
        <button @click="auth.logout" class="text-red-500 underline">Logout</button>
      </span>
      <RouterLink v-else to="/login" class="ml-auto">Login</RouterLink>
    </nav>
    <RouterView />
  </div>
</template>
