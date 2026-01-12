// API Configuration
// The VITE_API_URL env variable takes priority if set
// Otherwise, change CURRENT_ENV below to switch environments

const ENVIRONMENTS = {
    local: 'http://localhost:8080',       // Works on any PC for local development
    aws: 'http://13.60.208.216:8080',     // AWS EC2 Public IP
};

// ⚙️ CHANGE THIS VALUE TO SWITCH ENVIRONMENTS
const CURRENT_ENV = 'local';

export const API_URL = import.meta.env.VITE_API_URL || ENVIRONMENTS[CURRENT_ENV];

// Helper to check current environment
export const isLocalEnv = () => API_URL.includes('localhost') || API_URL.includes('127.0.0.1');
export const isAwsEnv = () => !isLocalEnv();
