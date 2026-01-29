<template>
    <div class="bg-neutral-900 border border-neutral-800 p-6 rounded-xl w-full">
      <h3 class="text-sm font-medium text-neutral-400 mb-4">Delivery status</h3>
      <Bar :data="chartData" :options="chartOptions" />
    </div>
  </template>
  
  <script setup>
  import { Bar } from 'vue-chartjs'
  import {
    Chart as ChartJS,
    Title,
    Tooltip,
    Legend,
    BarElement,
    CategoryScale,
    LinearScale
  } from 'chart.js'
  import { computed } from 'vue'
  
  ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)
  
  const props = defineProps({
    delivered: { type: Number, default: 0 },
    pending: { type: Number, default: 0 },
    active: { type: Number, default: 0 }
  })
  
  const chartData = computed(() => {
    const hasActive = props.active != null && props.active > 0
    return {
      labels: hasActive ? ['Delivered', 'In Transit', 'Requested'] : ['Delivered', 'Pending'],
      datasets: [
        {
          label: 'Deliveries',
          backgroundColor: hasActive
            ? ['#22c55e', '#a855f7', '#eab308']
            : ['#22c55e', '#525252'],
          data: hasActive
            ? [props.delivered ?? 0, props.active ?? 0, props.pending ?? 0]
            : [props.delivered ?? 0, props.pending ?? 0]
        }
      ]
    }
  })
  
  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          color: '#ffffff',
        },
      },
      title: {
        display: false,
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          precision: 0, // ensure integers
          color: '#ffffff',
        },
        grid: {
          color: 'rgba(255,255,255,0.12)'
        }
      },
      x: {
        ticks: {
          color: '#ffffff',
        },
        grid: {
          color: 'rgba(255,255,255,0.12)'
        }
      }
    }
  }
  </script>
  