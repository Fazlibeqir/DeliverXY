import { defineStore } from 'pinia'
import { rawApi } from '../services/http'
import { extractTokens } from '../services/authTokens'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    accessToken: null,
    refreshToken: null,
  }),
  getters: {
    isAuthenticated: (s) => Boolean(s.accessToken),
    isAdmin: (s) => {
      const u = s.user
      if (!u) return false
      // UserResponseDTO has role as UserRole enum (string)
      const role = u.role
      if (typeof role === 'string') return role.toUpperCase() === 'ADMIN'
      return false
    },
  },
  actions: {
    async login(email, password) {
      const res = await rawApi.post('/api/auth/login', { identifier: email, password })
      // Unwrap ApiResponse wrapper if present
      const authData = res.data?.data ?? res.data
      const { accessToken, refreshToken } = extractTokens(authData)

      if (!accessToken) throw new Error('Login did not return an access token')

      this.accessToken = accessToken
      if (refreshToken) this.refreshToken = refreshToken

      // AuthResponseDTO includes user, so use it if available, otherwise fetch
      if (authData?.user) {
        this.user = authData.user
      } else {
        await this.fetchMe()
      }

      // Admin panel: only allow ADMIN users
      if (!this.isAdmin) {
        this.clear()
        throw new Error('Not authorized (admin only)')
      }
    },
    async fetchMe() {
      if (!this.accessToken) return
      try {
        const res = await rawApi.get('/api/users/me', {
          headers: { Authorization: `Bearer ${this.accessToken}` },
        })
        this.user = res.data
      } catch (e) {
        this.clear()
        throw e
      }
    },
    async logout() {
      // Best-effort server logout; ignore failures.
      try {
        await rawApi.post('/api/auth/logout', this.refreshToken ? { refreshToken: this.refreshToken } : {})
      } catch {
        // ignore
      } finally {
        this.clear()
      }
    },
    clear() {
      this.user = null
      this.accessToken = null
      this.refreshToken = null
    }
  },
  persist: true,
})
