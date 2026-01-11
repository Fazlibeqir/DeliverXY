<template>
    <div class="bg-neutral-900 border border-neutral-800 p-6 rounded-xl shadow w-full md:w-1/2">
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
        backgroundColor: ['#ffffff', '#525252'],
        data: [props.delivered, props.pending]  // must be numbers
      }
    ]
  }))
  
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
  