<script setup>
import { onMounted, ref } from 'vue'
import api from '../services/axios'
import JsonViewer from './JsonViewer.vue'

const loading = ref(false)
const error = ref(null)
const earnings = ref(null)

async function fetchEarnings() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/earnings')
    earnings.value = res.data
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load earnings'
  } finally {
    loading.value = false
  }
}

onMounted(fetchEarnings)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">Earnings</h1>
        <p class="text-sm text-neutral-400">Admin earnings overview</p>
      </div>
      <button class="btn btn-secondary" @click="fetchEarnings" :disabled="loading">
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
    </div>

    <div v-if="error" class="p-4 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
      {{ error }}
    </div>

    <div class="card" v-if="earnings">
      <JsonViewer :value="earnings" />
    </div>
  </div>
</template>
