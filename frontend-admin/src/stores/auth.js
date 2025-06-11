import { defineStore } from 'pinia'
import api from '../services/axios'  // your axios instance with interceptor
import router from '../router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
  }),
  actions: {
    async login(email, password) {
      try {
        const res = await api.post('/auth/signin', { email, password })
        this.token = res.data.token
        console.log('Token being sent:', this.token)
        localStorage.setItem('token', this.token)
        await this.fetchUser()
        router.push('/')
        // no need for window.location.reload()
      } catch (error) {
        throw error
      }
    },
    async fetchUser() {
      if (!this.token|| this.token.split('.').length !== 3) {
        this.logout()
        return
      }
      try {
        const res = await api.get('/auth/me')  // your axios already sets token header
        this.user = res.data
      } catch {
        this.logout()
      }
    },
    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
      router.push('/login')
    }
  },
  persist: true,
})
