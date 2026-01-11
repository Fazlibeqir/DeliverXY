<template>
    <div class="bg-white p-6 rounded-xl shadow w-full md:w-1/2">
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
    delivered: Number,
    pending: Number
  })
  
  const chartData = computed(() => ({
    labels: ['Delivered', 'Pending'],
    datasets: [
      {
        label: 'Delivery Stats',
        backgroundColor: ['#8b5cf6', '#facc15'],
        data: [props.delivered, props.pending]  // must be numbers
      }
    ]
  }))
  
  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top'
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          precision: 0 // ensure integers
        }
      }
    }
  }
  </script>
  