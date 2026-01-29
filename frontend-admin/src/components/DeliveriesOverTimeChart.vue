<template>
  <div class="bg-neutral-900 border border-neutral-800 p-6 rounded-xl w-full">
    <h3 class="text-sm font-medium text-neutral-400 mb-4">Deliveries over time (last 14 days)</h3>
    <div v-if="!labels?.length || labels.every((_, i) => !data?.[i])" class="py-8 text-center text-neutral-500 text-sm">
      No delivery data for the last 14 days
    </div>
    <Line v-else :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title, Tooltip, Legend,
  LineElement, PointElement,
  LinearScale, CategoryScale
} from 'chart.js'
import { computed } from 'vue'

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, LinearScale, CategoryScale)

const props = defineProps({
  labels: Array,
  data: Array
})

const chartData = computed(() => ({
  labels: props.labels ?? [],
  datasets: [
    {
      label: 'Deliveries',
      backgroundColor: 'rgba(59, 130, 246, 0.2)',
      borderColor: '#3b82f6',
      data: props.data ?? [],
      tension: 0.35,
      fill: true,
      pointBackgroundColor: '#3b82f6'
    }
  ]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: {
      display: false
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        precision: 0,
        color: '#a3a3a3'
      },
      grid: {
        color: 'rgba(255,255,255,0.08)'
      }
    },
    x: {
      ticks: {
        color: '#a3a3a3',
        maxRotation: 45
      },
      grid: {
        color: 'rgba(255,255,255,0.08)'
      }
    }
  }
}
</script>
