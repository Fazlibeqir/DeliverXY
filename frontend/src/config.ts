// API Configuration for Mobile App
// ---------------------------------
// Uses environment variable API_URL if set, otherwise defaults to localhost
// For local dev: Set API_URL in .env or build command
// For AWS: Set API_URL=http://13.60.229.249:8080 in build command

export const API_URL = (process.env.API_URL as string) || 'http://localhost:8080';

// Always log the API URL on app start (helps debug connection issues)
console.log('=== API CONFIGURATION ===');
console.log('API_URL:', API_URL);
console.log('process.env.API_URL:', process.env.API_URL || '(not set)');
console.log('========================');

// Helper to check current environment
export const isLocalEnv = (): boolean => API_URL.includes('localhost') || API_URL.includes('127.0.0.1') || API_URL.includes('192.168.');
export const isAwsEnv = (): boolean => !isLocalEnv();
