import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
  }),
  actions: {
    async login(email, password) {
      try {
        const res = await axios.post('http://localhost:8080/api/auth/login', { email, password })
        this.token = res.data.token
        localStorage.setItem('token', this.token)
        await this.fetchUser()
      } catch (error) {
        throw error
      }
    },
    async fetchUser() {
      if (!this.token) return
      try {
        const res = await axios.get('http://localhost:8080/api/auth/user', {
          headers: { Authorization: `Bearer ${this.token}` },
        })
        this.user = res.data
      } catch {
        this.logout()
      }
    },
    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
    }
  }
})
