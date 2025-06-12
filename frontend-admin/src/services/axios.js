import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const api = axios.create({ 
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',

})

api.interceptors.request.use((config) => {
  const token = useAuthStore().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default api
