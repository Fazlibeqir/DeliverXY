import axios from 'axios'
import { unwrapApiResponse } from './apiResponse'
import { API_URL } from '../config'

const baseURL = API_URL

// No interceptors; safe to import anywhere (including stores).
export const rawApi = axios.create({ baseURL })

rawApi.interceptors.response.use((res) => {
  res.data = unwrapApiResponse(res.data)
  return res
})
