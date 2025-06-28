<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../services/axios'
import DashboardCard from './DashboardCard.vue'
import DeliveryStatsChart from './DeliveryStatsChart.vue'
// import DeliveriesOverTimeChart from './DeliveriesOverTimeChart.vue'

const userCount = ref(0)
const deliveryCount = ref(0)
const deliveredCount = ref(0)
const pendingCount = ref(0)
// const deliveryDates = ref([])

// const deliveriesByDate = computed(() => {
//   const counts = {}
//   deliveryDates.value.forEach(date => {
//     counts[date] = (counts[date] || 0) + 1
//   })
//   const sorted = Object.entries(counts).sort((a, b) => new Date(a[0]) - new Date(b[0]))
//   return {
//     labels: sorted.map(([date]) => date),
//     data: sorted.map(([_, count]) => count)
//   }
// })

async function fetchStats() {
  
  const users = await api.get('/user')
  const deliveries = await api.get('/deliveries')

  userCount.value = users.data.length
  deliveryCount.value = deliveries.data.length
  deliveredCount.value = deliveries.data.filter(d => d.status === 'DELIVERED').length
  pendingCount.value = deliveries.data.filter(d => d.status === 'PENDING').length
  //deliveryDates.value = deliveries.data.map(d => d.createdAt.split('T')[0])
}

onMounted(fetchStats)
</script>

<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <h1 class="text-3xl font-bold text-gray-800 mb-8">📊 Admin Dashboard and aws CD test2.0</h1>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <DashboardCard title="Total Users" :count="userCount" color="blue" />
      <DashboardCard title="Total Deliveries" :count="deliveryCount" color="green" />
      <DashboardCard title="Pending Deliveries" :count="pendingCount" color="yellow" />
      <DashboardCard title="Delivered" :count="deliveredCount" color="purple" />
    </div>

    <!-- Chart section -->
    <div class="mt-12">
      <DeliveryStatsChart :delivered="deliveredCount" :pending="pendingCount" />
    </div>

    <!-- 📊 Deliveries Over Time Chart -->
  <!-- <div class="mt-10">
  <DeliveriesOverTimeChart
    :labels="deliveriesByDate.labels"
    :data="deliveriesByDate.data"
  />
  </div> -->

    <div class="mt-10 flex gap-4">
      <RouterLink to="/users" class="btn btn-primary"> Manage Users</RouterLink>
      <RouterLink to="/deliveries" class="btn btn-success"> Manage Deliveries</RouterLink>
    </div>
  </div>
</template>
