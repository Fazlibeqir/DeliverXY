<script setup>
import axios from 'axios'
import { ref, onMounted } from 'vue'

const deliveries = ref([])

async function fetchDeliveries() {
  const res = await axios.get('http://localhost:8080/api/deliveries')
  deliveries.value = res.data
}

onMounted(fetchDeliveries)
</script>

<template>
  <div>
    <h1>Deliveries</h1>
    <ul>
      <li v-for="d in deliveries" :key="d.id">
        {{ d.description }} - Status: {{ d.status }}
      </li>
    </ul>
  </div>
</template>
