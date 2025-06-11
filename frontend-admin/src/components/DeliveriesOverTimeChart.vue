<template>
    <div class="bg-white p-6 rounded-xl shadow w-full">
      <Line :data="chartData" :options="chartOptions" />
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
  
  ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, LinearScale, CategoryScale)
  
  const props = defineProps({
    labels: Array,
    data: Array
  })
  
  const chartData = {
    labels: props.labels,
    datasets: [
      {
        label: 'Deliveries per Day',
        backgroundColor: '#3b82f6',
        borderColor: '#3b82f6',
        data: props.data,
        tension: 0.4,
        fill: false
      }
    ]
  }
  
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
        ticks: { precision: 0 }
      }
    }
  }
  </script>
  