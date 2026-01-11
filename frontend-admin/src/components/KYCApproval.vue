<script setup>
import api from '../services/axios'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const userId = ref(route.params.userId ? parseInt(route.params.userId) : null)
const kyc = ref(null)
const user = ref(null)
const loading = ref(false)
const error = ref(null)
const rejectionReason = ref('')
const showRejectModal = ref(false)
const processing = ref(false)

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const apiBaseURL = baseURL.replace(/\/$/, '') // Remove trailing slash

async function fetchKYC() {
  if (!userId.value) {
    error.value = 'User ID is required'
    return
  }

  loading.value = true
  error.value = null
  try {
    const kycRes = await api.get(`/api/admin/kyc/${userId.value}`)
    // Check if data exists (could be null if no KYC found)
    if (kycRes.data && Object.keys(kycRes.data).length > 0) {
      kyc.value = kycRes.data
    } else {
      kyc.value = null
      error.value = 'No KYC submission found for this user'
    }

    // Fetch user info
    try {
      const usersRes = await api.get('/api/admin/users', { params: { page: 0, size: 1000 } })
      const users = Array.isArray(usersRes.data) ? usersRes.data : usersRes.data?.content || []
      user.value = users.find(u => u.id === userId.value)
    } catch (userErr) {
      console.warn('Failed to fetch user info:', userErr)
    }
  } catch (e) {
    const errorMsg = e?.response?.data?.message || e?.response?.data?.error || e?.message || 'Failed to load KYC information'
    error.value = errorMsg
    kyc.value = null
    console.error('KYC fetch error:', e)
  } finally {
    loading.value = false
  }
}

function getImageUrl(url) {
  if (!url) return null
  
  // If already a full URL, return as is
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // If it's a relative path starting with /uploads, construct full URL
  if (url.startsWith('/uploads/')) {
    return `${apiBaseURL}${url}`
  }
  
  // If it doesn't start with /, add it
  if (!url.startsWith('/')) {
    return `${apiBaseURL}/${url}`
  }
  
  // Default: prepend base URL
  return `${apiBaseURL}${url}`
}

async function approveKYC() {
  if (!userId.value) return

  processing.value = true
  try {
    await api.post(`/api/admin/kyc/${userId.value}/approve`)
    alert('KYC approved successfully!')
    router.push('/users')
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to approve KYC'
  } finally {
    processing.value = false
  }
}

async function rejectKYC() {
  if (!userId.value || !rejectionReason.value.trim()) {
    alert('Please provide a rejection reason')
    return
  }

  processing.value = true
  try {
    // Backend expects the reason as a plain string in the request body
    await api.post(`/api/admin/kyc/${userId.value}/reject`, rejectionReason.value, {
      headers: {
        'Content-Type': 'text/plain'
      }
    })
    alert('KYC rejected successfully!')
    router.push('/users')
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Failed to reject KYC'
  } finally {
    processing.value = false
    showRejectModal.value = false
    rejectionReason.value = ''
  }
}

function openRejectModal() {
  showRejectModal.value = true
}

function closeRejectModal() {
  showRejectModal.value = false
  rejectionReason.value = ''
}

onMounted(() => {
  fetchKYC()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">KYC Approval</h1>
        <p class="text-sm text-neutral-400">
          Review and verify user identity documents
          <span v-if="user" class="ml-2">- {{ user.fullName || user.email }}</span>
        </p>
      </div>
      <button class="btn btn-secondary" @click="router.push('/users')">
        ← Back to Users
      </button>
    </div>

    <div v-if="error" class="p-4 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
      {{ error }}
    </div>

    <div v-if="loading" class="card text-center py-12">
      <div class="text-neutral-400">Loading KYC information...</div>
    </div>

    <div v-else-if="!kyc" class="card text-center py-12">
      <div class="text-neutral-400">No KYC submission found for this user</div>
    </div>

    <div v-else class="space-y-6">
      <!-- User Info Card -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">User Information</h2>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div>
            <div class="text-sm text-neutral-400">Name</div>
            <div class="text-white">{{ user?.fullName || '—' }}</div>
          </div>
          <div>
            <div class="text-sm text-neutral-400">Email</div>
            <div class="text-white">{{ user?.email || '—' }}</div>
          </div>
          <div>
            <div class="text-sm text-neutral-400">Status</div>
            <div class="text-white">
              <span v-if="kyc.status === 'APPROVED'" class="text-green-400">Approved</span>
              <span v-else-if="kyc.status === 'REJECTED'" class="text-red-400">Rejected</span>
              <span v-else class="text-yellow-400">Pending</span>
            </div>
          </div>
          <div>
            <div class="text-sm text-neutral-400">Submitted At</div>
            <div class="text-white">{{ kyc.submittedAt ? new Date(kyc.submittedAt).toLocaleString() : '—' }}</div>
          </div>
          <div v-if="kyc.reviewedBy">
            <div class="text-sm text-neutral-400">Reviewed By</div>
            <div class="text-white">{{ kyc.reviewedBy }}</div>
          </div>
          <div v-if="kyc.verifiedAt">
            <div class="text-sm text-neutral-400">Verified At</div>
            <div class="text-white">{{ new Date(kyc.verifiedAt).toLocaleString() }}</div>
          </div>
        </div>
      </div>

      <!-- Image Comparison Section -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Identity Verification</h2>
        <p class="text-sm text-neutral-400 mb-6">
          Compare the ID document photos with the selfie to verify the user's identity
        </p>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- ID Front -->
          <div class="space-y-2">
            <h3 class="font-medium text-neutral-300">ID Front</h3>
            <div class="border border-neutral-700 rounded-lg overflow-hidden bg-neutral-900">
              <img 
                v-if="getImageUrl(kyc.idFrontUrl)" 
                :src="getImageUrl(kyc.idFrontUrl)" 
                alt="ID Front"
                class="w-full h-auto object-contain max-h-96"
                @error="(e) => { console.error('ID Front image error:', e); e.target.style.display = 'none' }"
                @load="(e) => { console.log('ID Front loaded:', getImageUrl(kyc.idFrontUrl)) }"
              />
              <div v-else class="p-12 text-center text-neutral-500">
                No image available
              </div>
            </div>
          </div>

          <!-- ID Back -->
          <div class="space-y-2">
            <h3 class="font-medium text-neutral-300">ID Back</h3>
            <div class="border border-neutral-700 rounded-lg overflow-hidden bg-neutral-900">
              <img 
                v-if="getImageUrl(kyc.idBackUrl)" 
                :src="getImageUrl(kyc.idBackUrl)" 
                alt="ID Back"
                class="w-full h-auto object-contain max-h-96"
                @error="(e) => { console.error('ID Back image error:', e, getImageUrl(kyc.idBackUrl)); e.target.style.display = 'none' }"
                @load="(e) => { console.log('ID Back loaded:', getImageUrl(kyc.idBackUrl)) }"
              />
              <div v-else class="p-12 text-center text-neutral-500">
                No image available
              </div>
            </div>
          </div>
        </div>

        <!-- Selfie Comparison -->
        <div class="mt-6 space-y-2">
          <h3 class="font-medium text-neutral-300">Selfie (for comparison)</h3>
          <div class="border border-neutral-700 rounded-lg overflow-hidden bg-neutral-900 max-w-md">
            <img 
              v-if="getImageUrl(kyc.selfieUrl)" 
              :src="getImageUrl(kyc.selfieUrl)" 
              alt="Selfie"
              class="w-full h-auto object-contain max-h-96"
              @error="(e) => { console.error('Selfie image error:', e, getImageUrl(kyc.selfieUrl)); e.target.style.display = 'none' }"
              @load="(e) => { console.log('Selfie loaded:', getImageUrl(kyc.selfieUrl)) }"
            />
            <div v-else class="p-12 text-center text-neutral-500">
              No image available
            </div>
          </div>
        </div>

        <!-- Proof of Address (if available) -->
        <div v-if="kyc.proofOfAddressUrl" class="mt-6 space-y-2">
          <h3 class="font-medium text-neutral-300">Proof of Address</h3>
          <div class="border border-neutral-700 rounded-lg overflow-hidden bg-neutral-900 max-w-md">
            <img 
              :src="getImageUrl(kyc.proofOfAddressUrl)" 
              alt="Proof of Address"
              class="w-full h-auto object-contain max-h-96"
              @error="(e) => { console.error('Proof of Address image error:', e, getImageUrl(kyc.proofOfAddressUrl)); e.target.style.display = 'none' }"
              @load="(e) => { console.log('Proof of Address loaded:', getImageUrl(kyc.proofOfAddressUrl)) }"
            />
          </div>
        </div>
      </div>

      <!-- Rejection Reason (if rejected) -->
      <div v-if="kyc.status === 'REJECTED' && kyc.rejectionReason" class="card bg-red-950/20 border-red-900/50">
        <h3 class="font-medium text-red-300 mb-2">Rejection Reason</h3>
        <p class="text-red-200">{{ kyc.rejectionReason }}</p>
      </div>

      <!-- Action Buttons -->
      <div v-if="kyc.status === 'PENDING'" class="card">
        <div class="flex items-center gap-4">
          <button 
            class="btn btn-primary flex-1" 
            @click="approveKYC"
            :disabled="processing"
          >
            {{ processing ? 'Processing...' : '✓ Approve KYC' }}
          </button>
          <button 
            class="btn btn-secondary flex-1" 
            @click="openRejectModal"
            :disabled="processing"
          >
            ✗ Reject KYC
          </button>
        </div>
      </div>
    </div>

    <!-- Rejection Modal -->
    <div 
      v-if="showRejectModal" 
      class="fixed inset-0 bg-black/80 flex items-center justify-center z-50"
      @click.self="closeRejectModal"
    >
      <div class="card max-w-md w-full mx-4" @click.stop>
        <h3 class="text-lg font-semibold mb-4">Reject KYC</h3>
        <p class="text-sm text-neutral-400 mb-4">
          Please provide a reason for rejecting this KYC submission:
        </p>
        <textarea
          v-model="rejectionReason"
          class="input w-full h-32 mb-4"
          placeholder="Enter rejection reason..."
        ></textarea>
        <div class="flex gap-4">
          <button class="btn btn-secondary flex-1" @click="closeRejectModal">
            Cancel
          </button>
          <button 
            class="btn btn-primary flex-1 bg-red-600 hover:bg-red-700" 
            @click="rejectKYC"
            :disabled="processing || !rejectionReason.trim()"
          >
            {{ processing ? 'Processing...' : 'Reject' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
