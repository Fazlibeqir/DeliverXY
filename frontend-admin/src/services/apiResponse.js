export function isApiResponse(payload) {
  return (
    payload &&
    typeof payload === 'object' &&
    'success' in payload &&
    'data' in payload &&
    'timestamp' in payload
  )
}

export function unwrapApiResponse(payload) {
  return isApiResponse(payload) ? payload.data : payload
}

