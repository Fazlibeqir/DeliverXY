<script setup>
import { onMounted, ref } from 'vue'
import api from '../services/axios'

const loading = ref(false)
const error = ref(null)
const promoCodes = ref([])
const creating = ref(false)

const form = ref({
  code: '',
  description: '',
  discountType: 'PERCENTAGE',
  discountValue: 0,
  maxDiscountAmount: null,
  minOrderAmount: null,
  usageLimit: null,
  usagePerUser: 1,
  startDate: '',
  endDate: '',
  isActive: true,
  isFirstOrderOnly: false,
  applicableForNewUsersOnly: false,
})

async function fetchAll() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/promo-codes/all')
    // Handle ApiResponse wrapper - data might be in res.data.data or res.data
    const data = res.data?.data ?? res.data
    promoCodes.value = Array.isArray(data) ? data : []
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load promo codes'
    promoCodes.value = []
  } finally {
    loading.value = false
  }
}

async function deactivate(id) {
  await api.put(`/api/promo-codes/${id}/deactivate`)
  await fetchAll()
}

function toLocalDateTime(dtLocal) {
  if (!dtLocal) return null
  return dtLocal.length === 16 ? `${dtLocal}:00` : dtLocal
}

function nowDateTimeLocal() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function toNumberOrNull(v) {
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  return Number.isFinite(n) ? n : null
}

function toIntOrNull(v) {
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  return Number.isFinite(n) ? Math.trunc(n) : null
}

async function createPromo() {
  creating.value = true
  error.value = null
  try {
    const payload = {
      code: form.value.code,
      description: form.value.description,
      discountType: form.value.discountType,
      discountValue: toNumberOrNull(form.value.discountValue),
      maxDiscountAmount: toNumberOrNull(form.value.maxDiscountAmount),
      minOrderAmount: toNumberOrNull(form.value.minOrderAmount),
      usageLimit: toIntOrNull(form.value.usageLimit),
      usagePerUser: toIntOrNull(form.value.usagePerUser),
      startDate: toLocalDateTime(form.value.startDate),
      endDate: toLocalDateTime(form.value.endDate),
      isActive: Boolean(form.value.isActive),
      isFirstOrderOnly: Boolean(form.value.isFirstOrderOnly),
      applicableForNewUsersOnly: Boolean(form.value.applicableForNewUsersOnly),
    }

    const res = await api.post('/api/promo-codes/create', payload)
    console.log('Created promo code:', res.data)
    
    // Refresh the list after creation
    await fetchAll()

    form.value.code = ''
    form.value.description = ''
    form.value.discountType = 'PERCENTAGE'
    form.value.discountValue = 0
    form.value.maxDiscountAmount = null
    form.value.minOrderAmount = null
    form.value.usageLimit = null
    form.value.usagePerUser = 1
    form.value.startDate = nowDateTimeLocal()
    form.value.endDate = ''
    form.value.isActive = true
    form.value.isFirstOrderOnly = false
    form.value.applicableForNewUsersOnly = false
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Create failed'
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  if (!form.value.startDate) form.value.startDate = nowDateTimeLocal()
  fetchAll()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">Promo Codes</h1>
        <p class="text-sm text-neutral-400">Manage promotional codes</p>
      </div>
      <button class="btn btn-secondary" @click="fetchAll" :disabled="loading">
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
    </div>

    <div v-if="error" class="p-4 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
      {{ error }}
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Create Promo Code</h2>

        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium mb-2">Code *</label>
              <input class="input" v-model="form.code" placeholder="WELCOME10" required />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Description *</label>
              <input class="input" v-model="form.description" placeholder="10% off" required />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium mb-2">Discount Type *</label>
              <select class="input" v-model="form.discountType">
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED_AMOUNT">Fixed Amount (MKD)</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Discount Value *</label>
              <input class="input" type="number" step="0.01" min="0.01" v-model.number="form.discountValue" required />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium mb-2">Max Discount (optional)</label>
              <input class="input" type="number" step="0.01" min="0" v-model.number="form.maxDiscountAmount" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Min Order (optional)</label>
              <input class="input" type="number" step="0.01" min="0" v-model.number="form.minOrderAmount" />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium mb-2">Usage Limit (optional)</label>
              <input class="input" type="number" step="1" min="1" v-model.number="form.usageLimit" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">Usage Per User *</label>
              <input class="input" type="number" step="1" min="1" v-model.number="form.usagePerUser" required />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium mb-2">Start Date *</label>
              <input class="input" type="datetime-local" v-model="form.startDate" required />
            </div>
            <div>
              <label class="block text-sm font-medium mb-2">End Date (optional)</label>
              <input class="input" type="datetime-local" v-model="form.endDate" />
            </div>
          </div>

          <div class="space-y-2 pt-4 border-t border-neutral-800">
            <label class="flex items-center gap-2">
              <input type="checkbox" v-model="form.isActive" class="w-4 h-4" />
              <span class="text-sm">Active</span>
            </label>
            <label class="flex items-center gap-2">
              <input type="checkbox" v-model="form.isFirstOrderOnly" class="w-4 h-4" />
              <span class="text-sm">First Order Only</span>
            </label>
            <label class="flex items-center gap-2">
              <input type="checkbox" v-model="form.applicableForNewUsersOnly" class="w-4 h-4" />
              <span class="text-sm">New Users Only</span>
            </label>
          </div>

          <button 
            class="btn btn-primary w-full mt-4" 
            @click="createPromo" 
            :disabled="creating || !form.code || !form.description"
          >
            {{ creating ? 'Creating...' : 'Create' }}
          </button>
        </div>
      </div>

      <div class="card">
        <h2 class="text-lg font-semibold mb-4">All Promo Codes</h2>
        <div class="overflow-x-auto">
          <table class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Code</th>
                <th>Type</th>
                <th>Value</th>
                <th>Active</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in promoCodes" :key="p.id">
                <td>{{ p.id ?? '—' }}</td>
                <td class="font-mono">{{ p.code ?? '—' }}</td>
                <td>{{ p.discountType === 'PERCENTAGE' ? '%' : 'MKD' }}</td>
                <td>{{ p.discountValue ?? '—' }}</td>
                <td>{{ (p.isActive ?? p.active ?? true) ? 'Yes' : 'No' }}</td>
                <td>
                  <button 
                    class="btn btn-secondary text-xs" 
                    @click="deactivate(p.id)" 
                    :disabled="!p.id || !(p.isActive ?? p.active ?? true)"
                  >
                    Deactivate
                  </button>
                </td>
              </tr>
              <tr v-if="!loading && promoCodes.length === 0">
                <td colspan="6" class="text-center py-8 text-neutral-400">No promo codes found</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
