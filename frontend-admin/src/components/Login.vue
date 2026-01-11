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
    router.push('/') // redirect to dashboard on success
  } catch (e) {
    error.value = 'Invalid credentials'
  }
}
</script>

<template>
  <div class="login-form">
    <input v-model="email" placeholder="Email" />
    <input v-model="password" type="password" placeholder="Password" />
    <button @click="handleLogin">Login</button>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>
