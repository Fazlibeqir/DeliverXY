// API Configuration
// Uses VITE_API_URL environment variable (set at build time)
// Docker will inject this during build via ARG in Dockerfile
// For local dev: VITE_API_URL=http://localhost:8080
// For AWS: VITE_API_URL=http://13.60.229.249:8080

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Helper to check current environment
export const isLocalEnv = () => API_URL.includes('localhost') || API_URL.includes('127.0.0.1');
export const isAwsEnv = () => !isLocalEnv();
