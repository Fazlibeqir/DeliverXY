<script setup>
import api from '../services/axios'
import { ref, onMounted } from 'vue'

const deliveries = ref([])
const loading = ref(false)
const error = ref(null)
const page = ref(0)
const size = ref(25)

function normalizeListPayload(data) {
  if (Array.isArray(data)) return { items: data, total: data.length }
  if (Array.isArray(data?.content)) return { items: data.content, total: data.totalElements ?? data.content.length }
  if (Array.isArray(data?.items)) return { items: data.items, total: data.total ?? data.items.length }
  return { items: [], total: 0 }
}

async function fetchDeliveries() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/deliveries', { params: { page: page.value, size: size.value } })
    const { items } = normalizeListPayload(res.data)
    deliveries.value = items
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load deliveries'
    deliveries.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  try {
    return new Date(dateStr).toLocaleString()
  } catch {
    return dateStr
  }
}

function getStatusColor(status) {
  const colors = {
    REQUESTED: 'text-yellow-400',
    ASSIGNED: 'text-blue-400',
    IN_TRANSIT: 'text-purple-400',
    DELIVERED: 'text-green-400',
    CANCELLED: 'text-red-400',
  }
  return colors[status] || 'text-neutral-400'
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

    <div class="card">
      <div class="overflow-x-auto">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>From</th>
              <th>To</th>
              <th>Client</th>
              <th>Agent</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in deliveries" :key="d.id">
              <td>{{ d.id }}</td>
              <td>
                <div class="font-medium">{{ d.title || d.description || '—' }}</div>
                <div class="text-xs text-neutral-400" v-if="d.packageType">{{ d.packageType }}</div>
              </td>
              <td>
                <div class="text-sm">{{ d.pickupAddress || '—' }}</div>
                <div class="text-xs text-neutral-400" v-if="d.pickupContactName">{{ d.pickupContactName }}</div>
              </td>
              <td>
                <div class="text-sm">{{ d.dropoffAddress || '—' }}</div>
                <div class="text-xs text-neutral-400" v-if="d.dropoffContactName">{{ d.dropoffContactName }}</div>
              </td>
              <td>
                <div class="text-sm">{{ d.clientUsername || d.clientEmail || `#${d.clientId}` || '—' }}</div>
              </td>
              <td>
                <div class="text-sm" v-if="d.agentUsername || d.agentEmail">
                  {{ d.agentUsername || d.agentEmail || `#${d.agentId}` }}
                </div>
                <span v-else class="text-neutral-500 text-sm">Unassigned</span>
              </td>
              <td>
                <span :class="getStatusColor(d.status)">
                  {{ d.status || '—' }}
                </span>
              </td>
              <td>{{ formatDate(d.createdAt) }}</td>
            </tr>
            <tr v-if="!loading && deliveries.length === 0">
              <td colspan="8" class="text-center py-8 text-neutral-400">No deliveries found</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-4 flex items-center justify-between">
        <button class="btn btn-secondary" @click="prevPage" :disabled="loading || page === 0">Prev</button>
        <div class="text-sm text-neutral-400">Page {{ page + 1 }}</div>
        <button class="btn btn-secondary" @click="nextPage" :disabled="loading">Next</button>
      </div>
    </div>
  </div>
</template>
