<script setup>
import axios from 'axios'
import { ref, onMounted } from 'vue'

const users = ref([])

onMounted(async () => {
  const res = await axios.get('http://localhost:8080/api/admin/users')
  users.value = res.data
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
