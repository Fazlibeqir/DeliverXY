<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const password = ref('')
const error = ref(null)
const auth = useAuthStore()
const router = useRouter()

async function handleLogin() {
  error.value = null
  try {
    await auth.login(email.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = e?.message || 'Login failed'
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-black">
    <div class="w-full max-w-md card">
      <h1 class="text-2xl font-bold mb-2">Admin Login</h1>
      <p class="text-sm text-neutral-400 mb-6">Sign in with an admin account</p>

      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-2">Email</label>
          <input class="input" v-model="email" type="email" placeholder="admin@deliverxy.com" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-2">Password</label>
          <input class="input" v-model="password" type="password" placeholder="••••••••" />
        </div>
        <button class="btn btn-primary w-full" @click="handleLogin">Login</button>
      </div>

      <div v-if="error" class="mt-4 p-3 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
        {{ error }}
      </div>
    </div>
  </div>
</template>
