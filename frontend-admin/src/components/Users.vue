<script setup>
import api from '../services/axios'
import { ref, onMounted } from 'vue'

const users = ref([])
const loading = ref(false)
const error = ref(null)
const page = ref(0)
const size = ref(25)

async function fetchUsers() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/users', { params: { page: page.value, size: size.value } })
    const data = res.data
    users.value = Array.isArray(data) ? data : data?.content ?? data?.items ?? []
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load users'
    users.value = []
  } finally {
    loading.value = false
  }
}

function isBlocked(u) {
  // AdminUserDTO has isActive field
  return Boolean(u?.isActive === false)
}

async function blockUser(id) {
  await api.post(`/api/admin/users/${id}/block`)
  await fetchUsers()
}

async function unblockUser(id) {
  await api.post(`/api/admin/users/${id}/unblock`)
  await fetchUsers()
}

async function nextPage() {
  page.value += 1
  await fetchUsers()
}

async function prevPage() {
  page.value = Math.max(0, page.value - 1)
  await fetchUsers()
}

onMounted(fetchUsers)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">Users</h1>
        <p class="text-sm text-neutral-400">Manage system users</p>
      </div>
      <div class="flex items-center gap-2">
        <select class="input w-24" v-model.number="size" @change="fetchUsers">
          <option :value="10">10</option>
          <option :value="25">25</option>
          <option :value="50">50</option>
        </select>
        <button class="btn btn-secondary" @click="fetchUsers" :disabled="loading">
          {{ loading ? 'Loading...' : 'Refresh' }}
        </button>
      </div>
    </div>

    <div v-if="error" class="p-4 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
      {{ error }}
    </div>

    <div class="card">
      <div class="overflow-x-auto">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.fullName || '—' }}</td>
              <td>{{ u.email }}</td>
              <td>{{ u.role || '—' }}</td>
              <td>
                <span v-if="isBlocked(u)" class="text-red-400">Blocked</span>
                <span v-else class="text-green-400">Active</span>
              </td>
              <td>
                <button v-if="!isBlocked(u)" class="btn btn-secondary text-xs" @click="blockUser(u.id)">Block</button>
                <button v-else class="btn btn-primary text-xs" @click="unblockUser(u.id)">Unblock</button>
              </td>
            </tr>
            <tr v-if="!loading && users.length === 0">
              <td colspan="6" class="text-center py-8 text-neutral-400">No users found</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-4 flex items-center justify-between">
        <button class="btn btn-secondary" @click="prevPage" :disabled="loading || page === 0">Prev</button>
        <div class="text-sm text-neutral-400">Page {{ page + 1 }}</div>
        <button class="btn btn-secondary" @click="nextPage" :disabled="loading">Next</button>
      </div>
    </div>
  </div>
</template>
