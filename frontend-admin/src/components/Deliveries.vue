<script setup>
import api from '../services/axios'
import { ref, onMounted } from 'vue'
import JsonViewer from './JsonViewer.vue'

const deliveries = ref([])
const loading = ref(false)
const error = ref(null)
const page = ref(0)
const size = ref(25)
const assignAgentId = ref({})
const expanded = ref({})
const trackingByDeliveryId = ref({})

async function fetchDeliveries() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/deliveries', { params: { page: page.value, size: size.value } })
    const data = res.data
    deliveries.value = Array.isArray(data) ? data : data?.content ?? data?.items ?? []
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load deliveries'
    deliveries.value = []
  } finally {
    loading.value = false
  }
}

async function assignDelivery(deliveryId) {
  const agentId = assignAgentId.value[deliveryId]
  if (!agentId) return
  await api.post(`/api/admin/deliveries/${deliveryId}/assign`, { agentId })
  await fetchDeliveries()
}

async function toggleExpanded(deliveryId) {
  expanded.value[deliveryId] = !expanded.value[deliveryId]
  if (expanded.value[deliveryId] && trackingByDeliveryId.value[deliveryId] === undefined) {
    try {
      const res = await api.get(`/api/tracking/${deliveryId}`)
      trackingByDeliveryId.value[deliveryId] = res.data
    } catch (e) {
      trackingByDeliveryId.value[deliveryId] = {
        error: e?.response?.data?.message || e?.message || 'Failed to load tracking',
      }
    }
  }
}

async function nextPage() {
  page.value += 1
  await fetchDeliveries()
}

async function prevPage() {
  page.value = Math.max(0, page.value - 1)
  await fetchDeliveries()
}

onMounted(fetchDeliveries)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">Deliveries</h1>
        <p class="text-sm text-neutral-400">Manage delivery orders</p>
      </div>
      <div class="flex items-center gap-2">
        <select class="input w-24" v-model.number="size" @change="fetchDeliveries">
          <option :value="10">10</option>
          <option :value="25">25</option>
          <option :value="50">50</option>
        </select>
        <button class="btn btn-secondary" @click="fetchDeliveries" :disabled="loading">
          {{ loading ? 'Loading...' : 'Refresh' }}
        </button>
      </div>
    </div>

    <div v-if="error" class="p-4 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
      {{ error }}
    </div>

    <div class="space-y-3">
      <div v-for="d in deliveries" :key="d.id" class="card">
        <div class="flex items-center justify-between mb-3">
          <div>
            <div class="font-semibold">#{{ d.id }}</div>
            <div class="text-sm text-neutral-400">
              {{ d.status }} • {{ d.title || d.description || d.pickupAddress || d.dropoffAddress || '—' }}
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button class="btn btn-secondary text-xs" @click="toggleExpanded(d.id)">
              {{ expanded[d.id] ? 'Hide' : 'View' }} tracking
            </button>
            <input class="input w-32" v-model="assignAgentId[d.id]" placeholder="Agent ID" />
            <button class="btn btn-primary text-xs" @click="assignDelivery(d.id)">Assign</button>
          </div>
        </div>

        <div v-if="expanded[d.id]" class="mt-4 pt-4 border-t border-neutral-800">
          <div class="text-sm text-neutral-400 mb-2">Tracking data</div>
          <JsonViewer :value="trackingByDeliveryId[d.id]" />
        </div>
      </div>

      <div v-if="!loading && deliveries.length === 0" class="card text-center py-8 text-neutral-400">
        No deliveries found
      </div>
    </div>

    <div class="flex items-center justify-between">
      <button class="btn btn-secondary" @click="prevPage" :disabled="loading || page === 0">Prev</button>
      <div class="text-sm text-neutral-400">Page {{ page + 1 }}</div>
      <button class="btn btn-secondary" @click="nextPage" :disabled="loading">Next</button>
    </div>
  </div>
</template>
