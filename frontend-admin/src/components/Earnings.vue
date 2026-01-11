<script setup>
import { onMounted, ref } from 'vue'
import api from '../services/axios'
import DashboardCard from './DashboardCard.vue'

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

function formatCurrency(amount) {
  if (!amount && amount !== 0) return '0.00'
  return new Intl.NumberFormat('mk-MK', { 
    style: 'currency', 
    currency: 'MKD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

onMounted(fetchEarnings)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">Earnings</h1>
        <p class="text-sm text-neutral-400">Platform revenue and driver earnings overview</p>
      </div>
      <button class="btn btn-secondary" @click="fetchEarnings" :disabled="loading">
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
    </div>

    <div v-if="error" class="p-4 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
      {{ error }}
    </div>

    <div v-if="loading" class="card text-center py-12">
      <div class="text-neutral-400">Loading earnings data...</div>
    </div>

    <div v-else-if="earnings" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <DashboardCard 
          title="Platform Revenue" 
          :count="formatCurrency(earnings.totalPlatformRevenue || 0)" 
          color="green" 
        />
        <DashboardCard 
          title="Driver Earnings" 
          :count="formatCurrency(earnings.totalDriverEarnings || 0)" 
          color="blue" 
        />
        <DashboardCard 
          title="Total Deliveries" 
          :count="earnings.totalDelivered || 0" 
          color="purple" 
        />
      </div>

      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Earnings Breakdown</h2>
        <div class="space-y-4">
          <div class="flex items-center justify-between py-3 border-b border-neutral-800">
            <div>
              <div class="font-medium text-neutral-300">Platform Revenue</div>
              <div class="text-sm text-neutral-400">Total revenue earned by the platform</div>
            </div>
            <div class="text-xl font-bold text-green-400">
              {{ formatCurrency(earnings.totalPlatformRevenue || 0) }}
            </div>
          </div>
          
          <div class="flex items-center justify-between py-3 border-b border-neutral-800">
            <div>
              <div class="font-medium text-neutral-300">Total Driver Earnings</div>
              <div class="text-sm text-neutral-400">Total amount paid to drivers (including tips)</div>
            </div>
            <div class="text-xl font-bold text-blue-400">
              {{ formatCurrency(earnings.totalDriverEarnings || 0) }}
            </div>
          </div>

          <div class="flex items-center justify-between py-3">
            <div>
              <div class="font-medium text-neutral-300">Completed Deliveries</div>
              <div class="text-sm text-neutral-400">Total number of delivered orders</div>
            </div>
            <div class="text-xl font-bold text-purple-400">
              {{ earnings.totalDelivered || 0 }}
            </div>
          </div>

          <div v-if="earnings.totalDelivered > 0" class="mt-6 pt-6 border-t border-neutral-800">
            <div class="flex items-center justify-between">
              <div>
                <div class="font-medium text-neutral-300">Average Revenue per Delivery</div>
                <div class="text-sm text-neutral-400">Platform revenue divided by deliveries</div>
              </div>
              <div class="text-xl font-bold text-yellow-400">
                {{ formatCurrency((earnings.totalPlatformRevenue || 0) / (earnings.totalDelivered || 1)) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="card text-center py-12">
      <div class="text-neutral-400">No earnings data available</div>
    </div>
  </div>
</template>
