<script setup>
import api from '../services/axios'
import { ref, onMounted } from 'vue'

const deliveries = ref([])
const newDelivery = ref({ description:'', status:'PENDING' })

async function fetchDeliveries() {
  deliveries.value = (await api.get('/deliveries')).data
}

async function createDelivery() {
  await api.post('/deliveries', newDelivery.value)
  newDelivery.value = { description:'', status:'PENDING' }
  fetchDeliveries()
}

async function updateDelivery(d) {
  await api.put(`/deliveries/${d.id}`, d)
  fetchDeliveries()
}

async function deleteDelivery(id) {
  await api.delete(`/deliveries/${id}`)
  fetchDeliveries()
}

onMounted(fetchDeliveries)
</script>

<template>
  <div class="p-6 bg-white rounded-lg shadow-md max-w-xl mx-auto">
    <h2 class="text-2xl font-bold mb-4">🚚 Deliveries</h2>

    <form @submit.prevent="createDelivery" class="grid grid-cols-1 gap-4 mb-6">
      <input class="input" v-model="newDelivery.description" placeholder="Description" />
      <select class="input" v-model="newDelivery.status">
        <option>PENDING</option>
        <option>DELIVERED</option>
      </select>
      <button class="btn btn-primary w-full">Create</button>
    </form>

    <ul class="space-y-4">
      <li v-for="d in deliveries" :key="d.id" style="text-decoration: none;" class="bg-gray-50 p-4 rounded-lg shadow-sm">
        <input class="input mb-2 w-full" v-model="d.description" />

        <select class="input mb-2 w-full" v-model="d.status">
          <option>PENDING</option>
          <option>DELIVERED</option>
        </select>
        <div class="flex justify-end gap-2">
          <button class="btn btn-secondary" @click="updateDelivery(d)">💾 Save</button>
          <button class="btn btn-danger" @click="deleteDelivery(d.id)">🗑️ Delete</button>
        </div>
      </li>
    </ul>
  </div>
</template>

