// API Configuration for Mobile App
// ---------------------------------
// FORCED TO EC2 INSTANCE - NO FALLBACK
// This ensures the app ALWAYS connects to the production backend

// EC2 Instance IP (hard-coded for production)
const EC2_API_URL = 'http://13.60.229.249:8080';

// Force EC2 URL - no environment variable fallback
// This prevents protocol mismatches and connection issues
export const API_URL = EC2_API_URL;

// Validate URL format (must be HTTP, not HTTPS)
if (API_URL.startsWith('https://')) {
    console.error('ERROR: API_URL must use HTTP (not HTTPS) for EC2 backend');
    throw new Error('Invalid API_URL: HTTPS not supported for EC2 backend');
}

// Always log the API URL on app start (helps debug connection issues)
console.log('=== API CONFIGURATION (FORCED EC2) ===');
console.log('API_URL:', API_URL);
console.log('Protocol:', API_URL.startsWith('http://') ? 'HTTP ✓' : 'INVALID ✗');
console.log('========================');

// Helper to check current environment
export const isLocalEnv = (): boolean => false; // Always false - we're using EC2
export const isAwsEnv = (): boolean => true; // Always true - we're using EC2
