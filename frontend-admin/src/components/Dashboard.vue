<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../services/axios'
import DashboardCard from './DashboardCard.vue'
import DeliveryStatsChart from './DeliveryStatsChart.vue'
import DeliveriesOverTimeChart from './DeliveriesOverTimeChart.vue'

const userCount = ref(0)
const clientCount = ref(0)
const agentCount = ref(0)
const deliveryCount = ref(0)
const deliveredCount = ref(0)
const pendingCount = ref(0)
const activeCount = ref(0)
const pendingKYC = ref(0)
const platformRevenue = ref(null)
const driverEarnings = ref(null)
const deliveriesOverTimeLabels = ref([])
const deliveriesOverTimeData = ref([])
const loading = ref(true)
const chartLoading = ref(true)

function formatCurrency(amount) {
  if (amount == null || amount === undefined) return '—'
  return new Intl.NumberFormat('mk-MK', {
    style: 'currency',
    currency: 'MKD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

async function fetchStats() {
  loading.value = true
  const dash = await api.get('/api/admin/dashboard').catch(() => null)
  if (dash?.data) {
    const d = dash.data
    userCount.value = d.totalUsers ?? 0
    clientCount.value = d.totalClients ?? 0
    agentCount.value = d.totalAgents ?? 0
    deliveryCount.value = d.totalDeliveries ?? 0
    deliveredCount.value = d.completedDeliveries ?? 0
    pendingCount.value = d.pendingDeliveries ?? 0
    activeCount.value = d.activeDeliveries ?? 0
    pendingKYC.value = d.pendingKYC ?? 0
  }
  const earnings = await api.get('/api/admin/earnings').catch(() => null)
  if (earnings?.data) {
    platformRevenue.value = earnings.data.totalPlatformRevenue ?? 0
    driverEarnings.value = earnings.data.totalDriverEarnings ?? 0
  }
  loading.value = false
}

async function fetchDeliveriesOverTime() {
  chartLoading.value = true
  const days = 14
  const labels = []
  const counts = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    labels.push(d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }))
    counts.push(0)
  }
  try {
    const res = await api.get('/api/admin/deliveries', { params: { page: 0, size: 500 } })
    const raw = res.data
    const content = raw?.content ?? raw?.items ?? (Array.isArray(raw) ? raw : [])
    const list = Array.isArray(content) ? content : []
    list.forEach((delivery) => {
      const createdAt = delivery.createdAt
      if (!createdAt) return
      const date = new Date(createdAt)
      date.setHours(0, 0, 0, 0)
      const diffDays = Math.floor((today - date) / (1000 * 60 * 60 * 24))
      if (diffDays >= 0 && diffDays < days) {
        const idx = days - 1 - diffDays
        if (idx >= 0 && idx < counts.length) counts[idx] = (counts[idx] || 0) + 1
      }
    })
    deliveriesOverTimeLabels.value = labels
    deliveriesOverTimeData.value = counts
  } catch {
    deliveriesOverTimeLabels.value = labels
    deliveriesOverTimeData.value = counts
  } finally {
    chartLoading.value = false
  }
}

onMounted(async () => {
  await fetchStats()
  fetchDeliveriesOverTime()
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold mb-1">Admin Dashboard</h1>
      <p class="text-sm text-neutral-400">Overview of DeliverXY activity</p>
    </div>

    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="card animate-pulse h-24" />
      <div class="card animate-pulse h-24" />
      <div class="card animate-pulse h-24" />
      <div class="card animate-pulse h-24" />
    </div>

    <template v-else>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard title="Total Users" :count="userCount" color="blue" />
        <DashboardCard title="Total Deliveries" :count="deliveryCount" color="green" />
        <DashboardCard title="Pending Deliveries" :count="pendingCount" color="yellow" />
        <DashboardCard title="Pending KYC" :count="pendingKYC" color="orange" />
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard title="Platform Revenue" :count="formatCurrency(platformRevenue)" color="green" />
        <DashboardCard title="Driver Earnings" :count="formatCurrency(driverEarnings)" color="blue" />
        <DashboardCard title="Clients" :count="clientCount" color="blue" />
        <DashboardCard title="Agents" :count="agentCount" color="purple" />
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <DeliveryStatsChart
          :delivered="deliveredCount"
          :pending="pendingCount"
          :active="activeCount"
        />
        <div class="bg-neutral-900 border border-neutral-800 p-6 rounded-xl">
          <DeliveriesOverTimeChart
            v-if="!chartLoading"
            :labels="deliveriesOverTimeLabels"
            :data="deliveriesOverTimeData"
          />
          <div v-else class="py-12 text-center text-neutral-500 text-sm">
            Loading chart...
          </div>
        </div>
      </div>
    </template>

    <div v-if="pendingKYC > 0" class="p-4 bg-yellow-950/20 border border-yellow-900/50 rounded">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="font-semibold text-yellow-300">Pending KYC Reviews</h3>
          <p class="text-sm text-yellow-200/80">You have {{ pendingKYC }} KYC submission{{ pendingKYC !== 1 ? 's' : '' }} awaiting review</p>
        </div>
        <RouterLink to="/users" class="btn btn-primary">Review KYC</RouterLink>
      </div>
    </div>

    <div class="flex flex-wrap gap-3">
      <RouterLink to="/users" class="btn btn-primary">Manage Users</RouterLink>
      <RouterLink to="/deliveries" class="btn btn-primary">Manage Deliveries</RouterLink>
      <RouterLink to="/earnings" class="btn btn-secondary">Earnings</RouterLink>
      <RouterLink to="/payouts" class="btn btn-secondary">Payouts</RouterLink>
      <RouterLink to="/promo-codes" class="btn btn-secondary">Promo Codes</RouterLink>
      <RouterLink to="/pricing-config" class="btn btn-secondary">Pricing & Commission</RouterLink>
    </div>
  </div>
</template>
