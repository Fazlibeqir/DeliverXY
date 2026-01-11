<script setup>
import api from '../services/axios'
import { ref, onMounted, watch } from 'vue'

const users = ref([])
const filteredUsers = ref([])
const loading = ref(false)
const error = ref(null)
const page = ref(0)
const size = ref(25)
const searchQuery = ref('')
const kycFilter = ref('all') // 'all', 'pending', 'approved', 'rejected', 'none'

async function fetchUsers() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/users', { params: { page: page.value, size: size.value } })
    const data = res.data
    users.value = Array.isArray(data) ? data : data?.content ?? data?.items ?? []
    applyFilters()
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to load users'
    users.value = []
    filteredUsers.value = []
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  let filtered = [...users.value]
  
  // Apply search filter
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim()
    filtered = filtered.filter(u => 
      (u.fullName && u.fullName.toLowerCase().includes(query)) ||
      (u.email && u.email.toLowerCase().includes(query)) ||
      (u.id && u.id.toString().includes(query))
    )
  }
  
  // Apply KYC filter
  if (kycFilter.value !== 'all') {
    if (kycFilter.value === 'pending') {
      // Users with pending KYC
      filtered = filtered.filter(u => u.kycStatus === 'PENDING')
    } else if (kycFilter.value === 'approved') {
      filtered = filtered.filter(u => u.kycStatus === 'APPROVED' || u.kycVerified === true)
    } else if (kycFilter.value === 'rejected') {
      filtered = filtered.filter(u => u.kycStatus === 'REJECTED')
    } else if (kycFilter.value === 'none') {
      // Users with no KYC submission
      filtered = filtered.filter(u => !u.kycStatus && !u.kycVerified)
    }
  }
  
  filteredUsers.value = filtered
}

// Watch for filter changes
watch([searchQuery, kycFilter], () => {
  applyFilters()
})

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
      <div class="flex items-center gap-2 flex-wrap">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="Search by name, email, or ID..." 
          class="input flex-1 min-w-[200px]"
        />
        <select v-model="kycFilter" class="input w-40">
          <option value="all">All KYC Status</option>
          <option value="pending">Pending KYC</option>
          <option value="approved">Approved KYC</option>
          <option value="rejected">Rejected KYC</option>
          <option value="none">No KYC</option>
        </select>
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
            <tr v-for="u in filteredUsers" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.fullName || '—' }}</td>
              <td>{{ u.email }}</td>
              <td>{{ u.role || '—' }}</td>
              <td>
                <span v-if="isBlocked(u)" class="text-red-400">Blocked</span>
                <span v-else class="text-green-400">Active</span>
              </td>
              <td>
                <div class="flex items-center gap-2">
                  <router-link 
                    v-if="!u.kycVerified" 
                    :to="`/kyc/${u.id}`" 
                    class="btn btn-primary text-xs"
                  >
                    Review KYC
                  </router-link>
                  <button v-if="!isBlocked(u)" class="btn btn-secondary text-xs" @click="blockUser(u.id)">Block</button>
                  <button v-else class="btn btn-primary text-xs" @click="unblockUser(u.id)">Unblock</button>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && filteredUsers.length === 0 && users.length > 0">
              <td colspan="6" class="text-center py-8 text-neutral-400">No users match your filters</td>
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
