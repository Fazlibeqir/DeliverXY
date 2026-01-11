<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../services/axios'
import DashboardCard from './DashboardCard.vue'
import DeliveryStatsChart from './DeliveryStatsChart.vue'

const userCount = ref(0)
const deliveryCount = ref(0)
const deliveredCount = ref(0)
const pendingCount = ref(0)
const pendingKYC = ref(0)

async function fetchStats() {
  const dash = await api.get('/api/admin/dashboard').catch(() => null)
  if (dash?.data) {
    const d = dash.data // AdminDashboardDTO
    userCount.value = d.totalUsers ?? 0
    deliveryCount.value = d.totalDeliveries ?? 0
    deliveredCount.value = d.completedDeliveries ?? 0
    pendingCount.value = d.pendingDeliveries ?? 0
    pendingKYC.value = d.pendingKYC ?? 0
    return
  }

  // Fallback: derive from lists if dashboard endpoint unavailable
  try {
    const [usersRes, deliveriesRes] = await Promise.all([
      api.get('/api/admin/users', { params: { page: 0, size: 1 } }).catch(() => null),
      api.get('/api/admin/deliveries', { params: { page: 0, size: 1 } }).catch(() => null),
    ])

    // Note: This is just a fallback, actual counts would need totalElements from pagination
    userCount.value = 0
    deliveryCount.value = 0
    deliveredCount.value = 0
    pendingCount.value = 0
  } catch {
    // Silently ignore errors - user might not be authenticated
  }
}

onMounted(fetchStats)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold mb-1">Admin Dashboard</h1>
      <p class="text-sm text-neutral-400">Overview of DeliverXY activity</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <DashboardCard title="Total Users" :count="userCount" color="blue" />
      <DashboardCard title="Total Deliveries" :count="deliveryCount" color="green" />
      <DashboardCard title="Pending Deliveries" :count="pendingCount" color="yellow" />
      <DashboardCard title="Pending KYC" :count="pendingKYC" color="orange" />
    </div>

    <div class="mt-8">
      <DeliveryStatsChart :delivered="deliveredCount" :pending="pendingCount" />
    </div>

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
    </div>
  </div>
</template>
