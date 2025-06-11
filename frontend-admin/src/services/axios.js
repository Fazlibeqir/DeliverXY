import axios from 'axios'
import { useAuthStore } from '../stores/auth'


const api = axios.create({
  baseURL: 'http://localhost:8080/api',
})

// Inject token dynamically before each request
api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token && auth.token.split('.').length === 3) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

export default api

