<script setup>
import { onMounted, ref, computed } from 'vue'
import api from '../services/axios'

const loading = ref(false)
const error = ref(null)
const page = ref(0)
const size = ref(10)
const rows = ref([])
const processing = ref(new Set())
const showProcessModal = ref(null)
const transactionRef = ref('')

// Payout settings
const payoutSchedule = ref('manual')   // 'scheduled' | 'manual'
const scheduleFrequency = ref('weekly') // 'weekly' | 'biweekly' | 'monthly'
const processMode = ref('one_by_one')   // 'one_by_one' | 'all_at_once'
const showProcessAllModal = ref(false)
const processAllRef = ref('')
const processAllUseSameRef = ref(true)

// Example payout for demonstration
const examplePayout = {
  id: 999,
  driverId: 1,
  driverName: 'John Doe',
  driverEmail: 'driver@example.com',
  amountPaid: 253.59,
  periodStart: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
  periodEnd: new Date().toISOString(),
  status: 'PENDING',
  paidAt: null,
  transactionRef: null,
  processedBy: null
}

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
    rows.value = items.filter(p => p.id !== examplePayout.id)
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load payouts'
    rows.value = []
  } finally {
    loading.value = false
  }
}

const displayRows = computed(() => {
  if (rows.value.length === 0 && !loading.value) {
    return [examplePayout]
  }
  return rows.value
})

const pendingRows = computed(() =>
  displayRows.value.filter(p => p.status === 'PENDING' && p.id !== examplePayout.id)
)

const nextRunText = computed(() => {
  if (payoutSchedule.value !== 'scheduled') return null
  const d = new Date()
  if (scheduleFrequency.value === 'weekly') {
    const day = d.getDay()
    const toMonday = day === 0 ? 1 : day === 1 ? 0 : 8 - day
    d.setDate(d.getDate() + toMonday)
    d.setHours(9, 0, 0, 0)
    return `Next run: Monday ${d.toLocaleDateString()} at 09:00`
  }
  if (scheduleFrequency.value === 'biweekly') {
    d.setDate(1)
    d.setMonth(d.getMonth() + 1)
    d.setHours(9, 0, 0, 0)
    return `Next run: 1st of next month (${d.toLocaleDateString()}) at 09:00`
  }
  if (scheduleFrequency.value === 'monthly') {
    d.setDate(1)
    d.setMonth(d.getMonth() + 1)
    d.setHours(9, 0, 0, 0)
    return `Next run: 1st of next month (${d.toLocaleDateString()}) at 09:00`
  }
  return null
})

async function processPayout(payoutId, refOverride = null) {
  const ref = (refOverride ?? transactionRef.value)?.trim()
  if (!ref) {
    error.value = 'Transaction reference is required'
    return
  }
  processing.value.add(payoutId)
  error.value = null
  try {
    await api.post(`/api/admin/payouts/${payoutId}/process`, { transactionRef: ref })
    if (!refOverride) {
      showProcessModal.value = null
      transactionRef.value = ''
    }
    await fetchPayouts()
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to process payout'
  } finally {
    processing.value.delete(payoutId)
  }
}

async function processAllPending() {
  const list = pendingRows.value
  if (list.length === 0) return
  const useSame = processAllUseSameRef.value
  const baseRef = useSame ? (processAllRef.value?.trim() || `BATCH-${Date.now()}`) : null
  showProcessAllModal.value = false
  processAllRef.value = ''
  for (const p of list) {
    const ref = baseRef ?? `ADMIN-${p.id}-${Date.now()}`
    await processPayout(p.id, ref)
    if (error.value) break
  }
  await fetchPayouts()
}

function openProcessAllModal() {
  processAllRef.value = ''
  processAllUseSameRef.value = true
  showProcessAllModal.value = true
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  try {
    return new Date(dateStr).toLocaleString()
  } catch {
    return dateStr
  }
}

function formatCurrency(amount) {
  if (!amount) return '—'
  return new Intl.NumberFormat('mk-MK', { style: 'currency', currency: 'MKD' }).format(amount)
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
        <p class="text-sm text-neutral-400">Manage driver payouts</p>
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

    <!-- Payout settings -->
    <div class="card border border-neutral-700">
      <h2 class="text-lg font-semibold mb-4">Payout settings</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <h3 class="text-sm font-medium text-neutral-400 mb-2">When to run</h3>
          <div class="flex flex-wrap gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="payoutSchedule" value="manual" class="rounded" />
              <span>Manual</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="payoutSchedule" value="scheduled" class="rounded" />
              <span>Scheduled</span>
            </label>
          </div>
          <div v-if="payoutSchedule === 'scheduled'" class="mt-3">
            <label class="text-xs text-neutral-500 block mb-1">Frequency</label>
            <select v-model="scheduleFrequency" class="input w-40">
              <option value="weekly">Weekly</option>
              <option value="biweekly">Bi-weekly</option>
              <option value="monthly">Monthly</option>
            </select>
            <p v-if="nextRunText" class="text-xs text-neutral-400 mt-2">{{ nextRunText }}</p>
            <p class="text-xs text-neutral-500 mt-1">Schedule is for display; run payouts manually or via automation.</p>
          </div>
        </div>
        <div>
          <h3 class="text-sm font-medium text-neutral-400 mb-2">How to process</h3>
          <div class="flex flex-wrap gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="processMode" value="one_by_one" class="rounded" />
              <span>Agent by agent</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="processMode" value="all_at_once" class="rounded" />
              <span>All at once</span>
            </label>
          </div>
          <p class="text-xs text-neutral-500 mt-2">
            {{ processMode === 'one_by_one' ? 'Use the Process button on each row.' : 'Process all pending payouts on this page in one go.' }}
          </p>
          <button
            v-if="processMode === 'all_at_once' && pendingRows.length > 0"
            class="btn btn-primary mt-3"
            @click="openProcessAllModal"
            :disabled="processing.size > 0"
          >
            Process all pending ({{ pendingRows.length }})
          </button>
        </div>
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
              <th>Driver</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in displayRows" :key="p.id" :class="{ 'opacity-60': p.id === examplePayout.id }">
              <td>{{ p.id }}</td>
              <td>
                <div>
                  <div class="font-medium">{{ p.driverName || p.driverEmail || `Driver #${p.driverId}` }}</div>
                  <div class="text-xs text-neutral-400" v-if="p.driverEmail && p.driverName">{{ p.driverEmail }}</div>
                </div>
              </td>
              <td>{{ formatCurrency(p.amountPaid) }}</td>
              <td>
                <span
                  :class="{
                    'text-yellow-400': p.status === 'PENDING',
                    'text-green-400': p.status === 'PAID' || p.status === 'COMPLETED',
                    'text-red-400': p.status === 'FAILED' || p.status === 'CANCELLED',
                    'text-blue-400': p.status === 'PROCESSING'
                  }"
                >
                  {{ p.status || 'PENDING' }}
                </span>
              </td>
              <td>{{ formatDate(p.paidAt || p.periodEnd) }}</td>
              <td>
                <button
                  v-if="p.status === 'PENDING' && p.id !== examplePayout.id && processMode === 'one_by_one'"
                  class="btn btn-primary text-xs"
                  @click="showProcessModal = p.id; transactionRef = ''"
                  :disabled="processing.has(p.id)"
                >
                  Process
                </button>
                <span v-else-if="p.id === examplePayout.id" class="text-neutral-500 text-xs italic">Example</span>
                <span v-else-if="processMode === 'all_at_once' && p.status === 'PENDING'" class="text-neutral-500 text-xs">Use "Process all pending" above</span>
                <span v-else class="text-neutral-500 text-xs">{{ p.transactionRef || '—' }}</span>
              </td>
            </tr>
            <tr v-if="!loading && displayRows.length === 0">
              <td colspan="6" class="text-center py-8 text-neutral-400">No payouts found</td>
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

    <!-- Process single payout modal -->
    <div v-if="showProcessModal" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
      <div class="bg-neutral-800 p-8 rounded-lg shadow-lg w-full max-w-md">
        <h2 class="text-xl font-bold mb-4">Process Payout</h2>
        <p class="text-neutral-300 mb-4">
          Processing payout #{{ showProcessModal }}. This will deposit the amount to the driver's wallet.
        </p>
        <div class="mb-4">
          <label class="block text-sm font-medium mb-2">Transaction Reference *</label>
          <input
            v-model="transactionRef"
            class="input w-full"
            placeholder="e.g., BANK-TXN-12345"
          />
          <p class="text-xs text-neutral-400 mt-1">Enter the bank transaction reference or leave empty for auto-generated</p>
        </div>
        <div class="flex justify-end gap-4">
          <button class="btn btn-secondary" @click="showProcessModal = null; transactionRef = ''">Cancel</button>
          <button
            class="btn btn-primary"
            @click="processPayout(showProcessModal)"
            :disabled="processing.has(showProcessModal) || !transactionRef.trim()"
          >
            {{ processing.has(showProcessModal) ? 'Processing...' : 'Process Payout' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Process all pending modal -->
    <div v-if="showProcessAllModal" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
      <div class="bg-neutral-800 p-8 rounded-lg shadow-lg w-full max-w-md">
        <h2 class="text-xl font-bold mb-4">Process all pending payouts</h2>
        <p class="text-neutral-300 mb-4">
          This will process {{ pendingRows.length }} pending payout(s) on this page. Each will be marked as paid.
        </p>
        <div class="mb-4">
          <label class="flex items-center gap-2 cursor-pointer mb-2">
            <input type="checkbox" v-model="processAllUseSameRef" class="rounded" />
            <span class="text-sm">Use same transaction reference for all</span>
          </label>
          <input
            v-model="processAllRef"
            class="input w-full"
            placeholder="e.g., BATCH-2024-001"
          />
          <p class="text-xs text-neutral-400 mt-1">If unchecked, a unique ref will be generated per payout.</p>
        </div>
        <div class="flex justify-end gap-4">
          <button class="btn btn-secondary" @click="showProcessAllModal = false">Cancel</button>
          <button
            class="btn btn-primary"
            @click="processAllPending()"
            :disabled="processing.size > 0"
          >
            Process {{ pendingRows.length }} payout(s)
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
