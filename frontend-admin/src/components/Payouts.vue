<script setup>
import { onMounted, ref } from 'vue'
import api from '../services/axios'

const loading = ref(false)
const error = ref(null)
const page = ref(0)
const size = ref(10)
const rows = ref([])

function normalizeListPayload(data) {
  if (Array.isArray(data)) return { items: data, total: data.length }
  if (Array.isArray(data?.content)) return { items: data.content, total: data.totalElements ?? data.content.length }
  if (Array.isArray(data?.items)) return { items: data.items, total: data.total ?? data.items.length }
  return { items: [], total: 0 }
}

async function fetchPayouts() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/payouts', { params: { page: page.value, size: size.value } })
    const { items } = normalizeListPayload(res.data)
    rows.value = items
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load payouts'
  } finally {
    loading.value = false
  }
}

async function nextPage() {
  page.value += 1
  await fetchPayouts()
}

async function prevPage() {
  page.value = Math.max(0, page.value - 1)
  await fetchPayouts()
}

onMounted(fetchPayouts)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">Payouts</h1>
        <p class="text-sm text-neutral-400">Manage user payouts</p>
      </div>
      <div class="flex items-center gap-2">
        <select class="input w-24" v-model.number="size" @change="fetchPayouts">
          <option :value="10">10</option>
          <option :value="25">25</option>
          <option :value="50">50</option>
        </select>
        <button class="btn btn-secondary" @click="fetchPayouts" :disabled="loading">
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
              <th>User</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in rows" :key="p.id">
              <td>{{ p.id }}</td>
              <td>{{ p.driverId ?? '—' }}</td>
              <td>{{ p.amountPaid ?? p.amount ?? '—' }}</td>
              <td>{{ p.status ?? '—' }}</td>
              <td>{{ p.paidAt ?? p.createdAt ?? '—' }}</td>
            </tr>
            <tr v-if="!loading && rows.length === 0">
              <td colspan="5" class="text-center py-8 text-neutral-400">No payouts found</td>
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
