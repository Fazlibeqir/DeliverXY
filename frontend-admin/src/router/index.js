import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import Dashboard from '../components/Dashboard.vue'
import Users from '../components/Users.vue'
import Login from '../components/Login.vue'  // create this component

const routes = [
  { path: '/', component: Dashboard, meta: { requiresAuth: true } },
  { path: '/users', component: Users, meta: { requiresAuth: true } },
  { path: '/login', component: Login },
  { path: '/:pathMatch(.*)*', redirect: '/' } // catch-all redirect to dashboard or 404 page
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation Guard
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  
  if (to.meta.requiresAuth && !auth.token) {
    // Not authenticated and trying to access protected route
    next('/login')
  } else if (to.path === '/login' && auth.token) {
    // Authenticated user trying to access login page, redirect to dashboard
    next('/')
  } else {
    next()
  }
})

export default router
