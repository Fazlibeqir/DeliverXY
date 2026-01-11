// Extract tokens from AuthResponseDTO
// AuthResponseDTO: { accessToken, refreshToken, expiresIn, user }
export function extractTokens(data) {
  const accessToken = data?.accessToken ?? data?.token ?? data?.access_token ?? data?.jwt ?? null
  const refreshToken = data?.refreshToken ?? data?.refresh_token ?? null
  return { accessToken, refreshToken }
}

