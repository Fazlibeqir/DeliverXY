<script setup>
const props = defineProps({
  value: { type: [Object, Array, String, Number, Boolean, null], default: null },
})
</script>

<template>
  <pre class="bg-neutral-900 border border-neutral-800 rounded-xl p-4 overflow-auto text-xs text-neutral-200">
{{ typeof value === 'string' ? value : JSON.stringify(value, null, 2) }}
  </pre>
</template>

