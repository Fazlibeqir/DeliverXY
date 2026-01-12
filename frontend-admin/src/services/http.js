import axios from 'axios'
import { unwrapApiResponse } from './apiResponse'

const baseURL = import.meta.env.VITE_API_URL || 'http://13.60.225.179:8080'

// No interceptors; safe to import anywhere (including stores).
export const rawApi = axios.create({ baseURL })

rawApi.interceptors.response.use((res) => {
  res.data = unwrapApiResponse(res.data)
  return res
})
