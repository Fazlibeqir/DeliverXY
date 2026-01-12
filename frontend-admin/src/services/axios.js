import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { extractTokens } from './authTokens'
import { rawApi } from './http'
import { unwrapApiResponse } from './apiResponse'

const baseURL = import.meta.env.VITE_API_URL || 'http://13.60.225.179:8080'

const api = axios.create({ baseURL })

api.interceptors.request.use((config) => {
  const token = useAuthStore().accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let refreshPromise = null

api.interceptors.response.use(
  (res) => {
    res.data = unwrapApiResponse(res.data)
    return res
  },
  async (error) => {
    const auth = useAuthStore()
    const original = error?.config

    const status = error?.response?.status
    const is401 = status === 401
    const is403 = status === 403
    const alreadyRetried = Boolean(original?._retry)
    const isAuthRoute =
      typeof original?.url === 'string' &&
      (original.url.includes('/api/auth/login') || original.url.includes('/api/auth/refresh'))

    // Handle 403 Forbidden - might be expired token or missing permissions
    if (is403 && !isAuthRoute && auth.refreshToken && !alreadyRetried) {
      // Try to refresh token on 403 as well (might be expired token)
      original._retry = true
      try {
        refreshPromise =
          refreshPromise ||
          rawApi.post('/api/auth/refresh', auth.refreshToken ? { refreshToken: auth.refreshToken } : {})

        const refreshRes = await refreshPromise
        const { accessToken, refreshToken } = extractTokens(refreshRes.data)

        if (accessToken) {
          auth.accessToken = accessToken
          if (refreshToken) auth.refreshToken = refreshToken

          original.headers = original.headers || {}
          original.headers.Authorization = `Bearer ${accessToken}`

          return api(original)
        }
      } catch (e) {
        // Refresh failed, logout and reject
        auth.logout?.()
        return Promise.reject(error)
      } finally {
        refreshPromise = null
      }
    }

    // Handle 401 Unauthorized
    if (is401 && !alreadyRetried && !isAuthRoute && auth.refreshToken) {
      original._retry = true

      try {
        refreshPromise =
          refreshPromise ||
          rawApi.post('/api/auth/refresh', auth.refreshToken ? { refreshToken: auth.refreshToken } : {})

        const refreshRes = await refreshPromise
        const { accessToken, refreshToken } = extractTokens(refreshRes.data)

        if (!accessToken) throw new Error('Refresh did not return an access token')

        auth.accessToken = accessToken
        if (refreshToken) auth.refreshToken = refreshToken

        original.headers = original.headers || {}
        original.headers.Authorization = `Bearer ${accessToken}`

        return api(original)
      } catch (e) {
        auth.logout?.()
        return Promise.reject(error)
      } finally {
        refreshPromise = null
      }
    }

    // For 403 without refresh token or after retry, it's likely a permissions issue
    if (is403 && !auth.refreshToken) {
      // Not logged in, redirect will be handled by router guard
      auth.clear()
    }

    // For 403 after retry, it's a permissions issue - clear auth and let router handle redirect
    if ((is403 || is401) && alreadyRetried) {
      auth.clear()
      // Don't log these errors - they're expected when not authenticated
      error.silent = true
    }

    return Promise.reject(error)
  },
)

export default api
