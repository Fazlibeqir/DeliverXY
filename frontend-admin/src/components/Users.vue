<script setup>
import api from '../services/axios'
import { ref, onMounted } from 'vue'

const users = ref([])

onMounted(async () => {
  try {
    const res = await api.get('/user') // no need to specify full URL
    users.value = res.data
  } catch (err) {
    console.error('Failed to fetch users:', err)
  }
})
</script>

<template>
  <div class="p-4">
    <h1 class="text-xl font-bold mb-4">Users</h1>
    <ul>
      <li v-for="u in users" :key="u.id">{{ u.name }} - {{ u.email }}</li>
    </ul>
  </div>
</template>
