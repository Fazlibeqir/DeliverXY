import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import Dashboard from '../components/Dashboard.vue'
import Users from '../components/Users.vue'
import Login from '../components/Login.vue'
import Deliveries from '../components/Deliveries.vue'
import DeliveryMap from '../components/DeliveryMap.vue'
import Earnings from '../components/Earnings.vue'
import Payouts from '../components/Payouts.vue'
import PromoCodes from '../components/PromoCodes.vue'

const routes = [
  { path: '/', component: Dashboard, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/users', component: Users, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/deliveries', component: Deliveries, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/map', component: DeliveryMap, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/earnings', component: Earnings, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/payouts', component: Payouts, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/promo-codes', component: PromoCodes, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/login', component: Login },
  { path: '/:pathMatch(.*)*', redirect: '/' } // catch-all redirect to dashboard or 404 page
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation Guard
router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()
  
  // If we have a token but no user loaded yet, hydrate user
  if (auth.accessToken && !auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      // ignore; guard logic below will redirect
    }
  }

  if (to.meta.requiresAuth && !auth.accessToken) {
    // Not authenticated and trying to access protected route
    next('/login')
  } else if (to.meta.requiresAdmin && auth.accessToken && !auth.isAdmin) {
    await auth.logout()
    next('/login')
  } else if (to.path === '/login' && auth.accessToken) {
    // Authenticated user trying to access login page, redirect to dashboard
    next('/')
  } else {
    next()
  }
})

export default router
