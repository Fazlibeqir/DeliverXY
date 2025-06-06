import axios from 'axios'
import { useAuthStore } from './stores/auth'

axios.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})
