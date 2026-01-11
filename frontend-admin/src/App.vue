<script setup>
import { RouterView, RouterLink } from 'vue-router'
import './assets/tailwind.css';
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <nav class="border-b border-neutral-800 bg-neutral-900">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-8">
            <RouterLink to="/" class="text-lg font-bold text-white">
              DeliverXY Admin
            </RouterLink>
            <template v-if="auth.isAuthenticated">
              <div class="flex items-center gap-4">
                <RouterLink 
                  to="/" 
                  class="text-sm text-neutral-400 hover:text-white"
                  :class="{ 'text-white': $route.path === '/' }"
                >
                  Dashboard
                </RouterLink>
                <RouterLink 
                  to="/deliveries" 
                  class="text-sm text-neutral-400 hover:text-white"
                  :class="{ 'text-white': $route.path === '/deliveries' }"
                >
                  Deliveries
                </RouterLink>
                <RouterLink 
                  to="/users" 
                  class="text-sm text-neutral-400 hover:text-white"
                  :class="{ 'text-white': $route.path === '/users' }"
                >
                  Users
                </RouterLink>
                <RouterLink 
                  to="/earnings" 
                  class="text-sm text-neutral-400 hover:text-white"
                  :class="{ 'text-white': $route.path === '/earnings' }"
                >
                  Earnings
                </RouterLink>
                <RouterLink 
                  to="/payouts" 
                  class="text-sm text-neutral-400 hover:text-white"
                  :class="{ 'text-white': $route.path === '/payouts' }"
                >
                  Payouts
                </RouterLink>
                <RouterLink 
                  to="/promo-codes" 
                  class="text-sm text-neutral-400 hover:text-white"
                  :class="{ 'text-white': $route.path === '/promo-codes' }"
                >
                  Promo Codes
                </RouterLink>
              </div>
            </template>
          </div>

          <div class="flex items-center gap-4">
            <template v-if="auth.isAuthenticated">
              <span class="text-sm text-neutral-400">{{ auth.user?.email || auth.user?.username }}</span>
              <button class="btn btn-secondary" @click="handleLogout">Logout</button>
            </template>
            <template v-else>
              <RouterLink to="/login" class="btn btn-primary">Login</RouterLink>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto px-6 py-8">
      <RouterView />
    </main>
  </div>
</template>
