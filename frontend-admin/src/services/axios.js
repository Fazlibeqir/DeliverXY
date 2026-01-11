import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { extractTokens } from './authTokens'
import { rawApi } from './http'
import { unwrapApiResponse } from './apiResponse'

const baseURL = import.meta.env.VITE_API_URL

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
    const alreadyRetried = Boolean(original?._retry)
    const isAuthRoute =
      typeof original?.url === 'string' &&
      (original.url.includes('/api/auth/login') || original.url.includes('/api/auth/refresh'))

    if (!is401 || alreadyRetried || isAuthRoute || !auth.refreshToken) {
      return Promise.reject(error)
    }

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
      return Promise.reject(e)
    } finally {
      refreshPromise = null
    }
  },
)

export default api
