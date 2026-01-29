<script setup>
import { onMounted, ref, computed } from 'vue'
import api from '../services/axios'

const loading = ref(false)
const saving = ref(false)
const error = ref(null)
const configs = ref([])
const selected = ref(null)
const form = ref({})

const hasChanges = computed(() => {
  if (!selected.value || !form.value.id) return false
  const c = selected.value
  const f = form.value
  return (
    (f.name !== c.name) ||
    (f.city !== c.city) ||
    (f.currency !== c.currency) ||
    (Number(f.baseFare) !== Number(c.baseFare)) ||
    (Number(f.perKmRate) !== Number(c.perKmRate)) ||
    (Number(f.perMinuteRate) !== Number(c.perMinuteRate)) ||
    (Number(f.minimumFare) !== Number(c.minimumFare)) ||
    (Number(f.surgeMultiplier) !== Number(c.surgeMultiplier)) ||
    (Number(f.cityCenterMultiplier) !== Number(c.cityCenterMultiplier)) ||
    (Number(f.airportSurcharge) !== Number(c.airportSurcharge)) ||
    (Number(f.nightMultiplier) !== Number(c.nightMultiplier)) ||
    (Number(f.weekendMultiplier) !== Number(c.weekendMultiplier)) ||
    (Number(f.peakHourMultiplier) !== Number(c.peakHourMultiplier)) ||
    (f.isActive !== c.isActive) ||
    (Number(f.platformCommissionPercent) !== Number(c.platformCommissionPercent)) ||
    ((f.description || '') !== (c.description || ''))
  )
})

function toNum(v) {
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  return Number.isFinite(n) ? n : null
}

async function fetchAll() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/pricing-config')
    const data = res.data?.data ?? res.data
    configs.value = Array.isArray(data) ? data : []
    if (configs.value.length && !selected.value) {
      selectConfig(configs.value[0])
    }
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load pricing config'
    configs.value = []
  } finally {
    loading.value = false
  }
}

function selectConfig(c) {
  selected.value = c
  form.value = {
    id: c.id,
    name: c.name ?? '',
    city: c.city ?? '',
    currency: c.currency ?? '',
    baseFare: c.baseFare ?? '',
    perKmRate: c.perKmRate ?? '',
    perMinuteRate: c.perMinuteRate ?? '',
    minimumFare: c.minimumFare ?? '',
    surgeMultiplier: c.surgeMultiplier ?? '',
    cityCenterMultiplier: c.cityCenterMultiplier ?? '',
    airportSurcharge: c.airportSurcharge ?? '',
    nightMultiplier: c.nightMultiplier ?? '',
    weekendMultiplier: c.weekendMultiplier ?? '',
    peakHourMultiplier: c.peakHourMultiplier ?? '',
    isActive: c.isActive ?? true,
    platformCommissionPercent: c.platformCommissionPercent ?? '',
    description: c.description ?? '',
  }
}

async function save() {
  if (!form.value.id) return
  saving.value = true
  error.value = null
  try {
    const payload = {
      name: form.value.name,
      city: form.value.city,
      currency: form.value.currency,
      baseFare: toNum(form.value.baseFare),
      perKmRate: toNum(form.value.perKmRate),
      perMinuteRate: toNum(form.value.perMinuteRate),
      minimumFare: toNum(form.value.minimumFare),
      surgeMultiplier: toNum(form.value.surgeMultiplier),
      cityCenterMultiplier: toNum(form.value.cityCenterMultiplier),
      airportSurcharge: toNum(form.value.airportSurcharge),
      nightMultiplier: toNum(form.value.nightMultiplier),
      weekendMultiplier: toNum(form.value.weekendMultiplier),
      peakHourMultiplier: toNum(form.value.peakHourMultiplier),
      isActive: Boolean(form.value.isActive),
      platformCommissionPercent: toNum(form.value.platformCommissionPercent),
      description: form.value.description || null,
    }
    const res = await api.put(`/api/admin/pricing-config/${form.value.id}`, payload)
    const updated = res.data?.data ?? res.data
    const idx = configs.value.findIndex((c) => c.id === updated.id)
    if (idx >= 0) configs.value[idx] = updated
    selected.value = updated
    selectConfig(updated)
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Save failed'
  } finally {
    saving.value = false
  }
}

onMounted(fetchAll)
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Pricing & Commission</h1>

    <p class="text-neutral-400 text-sm mb-6">
      Configure base prices, per-km/per-minute rates, multipliers, and platform commission (cut). Changes apply to new deliveries.
    </p>

    <div v-if="error" class="mb-4 p-3 rounded bg-red-900/50 text-red-200 text-sm">
      {{ error }}
    </div>

    <div v-if="loading" class="text-neutral-400">Loading…</div>

    <div v-else-if="!configs.length" class="text-neutral-400">
      No pricing configs found.
    </div>

    <div v-else class="flex flex-col lg:flex-row gap-6">
      <div class="lg:w-56 shrink-0">
        <label class="text-xs uppercase text-neutral-500 font-medium mb-2 block">Config</label>
        <div class="space-y-1">
          <button
            v-for="c in configs"
            :key="c.id"
            type="button"
            class="block w-full text-left px-3 py-2 rounded text-sm transition"
            :class="selected?.id === c.id
              ? 'bg-neutral-700 text-white'
              : 'text-neutral-400 hover:bg-neutral-800 hover:text-white'"
            @click="selectConfig(c)"
          >
            {{ c.name || `Config #${c.id}` }}
            <span v-if="c.isActive" class="text-emerald-500 text-xs ml-1">active</span>
          </button>
        </div>
      </div>

      <div class="flex-1 min-w-0">
        <form v-if="selected" class="space-y-6" @submit.prevent="save">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm text-neutral-400 mb-1">Name</label>
              <input v-model="form.name" type="text" class="input w-full" placeholder="e.g. Standard Skopje" />
            </div>
            <div>
              <label class="block text-sm text-neutral-400 mb-1">City</label>
              <input v-model="form.city" type="text" class="input w-full" placeholder="Skopje" />
            </div>
            <div>
              <label class="block text-sm text-neutral-400 mb-1">Currency</label>
              <input v-model="form.currency" type="text" class="input w-full" placeholder="MKD" />
            </div>
            <div>
              <label class="block text-sm text-neutral-400 mb-1">Active</label>
              <select v-model="form.isActive" class="input w-full">
                <option :value="true">Yes</option>
                <option :value="false">No</option>
              </select>
            </div>
          </div>

          <div>
            <h2 class="text-sm font-semibold text-neutral-300 mb-3">Base prices & rates</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Base fare</label>
                <input v-model="form.baseFare" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Per km rate</label>
                <input v-model="form.perKmRate" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Per minute rate</label>
                <input v-model="form.perMinuteRate" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Minimum fare</label>
                <input v-model="form.minimumFare" type="number" step="0.01" min="0" class="input w-full" />
              </div>
            </div>
          </div>

          <div>
            <h2 class="text-sm font-semibold text-neutral-300 mb-3">Multipliers & surcharges</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Surge multiplier</label>
                <input v-model="form.surgeMultiplier" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">City center multiplier</label>
                <input v-model="form.cityCenterMultiplier" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Airport surcharge</label>
                <input v-model="form.airportSurcharge" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Night multiplier</label>
                <input v-model="form.nightMultiplier" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Weekend multiplier</label>
                <input v-model="form.weekendMultiplier" type="number" step="0.01" min="0" class="input w-full" />
              </div>
              <div>
                <label class="block text-sm text-neutral-400 mb-1">Peak hour multiplier</label>
                <input v-model="form.peakHourMultiplier" type="number" step="0.01" min="0" class="input w-full" />
              </div>
            </div>
          </div>

          <div>
            <h2 class="text-sm font-semibold text-neutral-300 mb-3">Platform commission (cut)</h2>
            <div class="max-w-xs">
              <label class="block text-sm text-neutral-400 mb-1">Platform commission %</label>
              <input
                v-model="form.platformCommissionPercent"
                type="number"
                step="0.1"
                min="0"
                max="100"
                class="input w-full"
                placeholder="20"
              />
              <p class="text-xs text-neutral-500 mt-1">
                Percentage of delivery fare kept by the platform. Driver receives the remainder (e.g. 20% → driver gets 80%).
              </p>
            </div>
          </div>

          <div>
            <label class="block text-sm text-neutral-400 mb-1">Description</label>
            <input v-model="form.description" type="text" class="input w-full" placeholder="Optional" />
          </div>

          <div class="flex items-center gap-4">
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="saving || !hasChanges"
            >
              {{ saving ? 'Saving…' : 'Save changes' }}
            </button>
            <span v-if="hasChanges && !saving" class="text-xs text-amber-400">Unsaved changes</span>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
