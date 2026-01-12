// API Configuration for Mobile App
// ---------------------------------
// LOCAL OPTIONS (pick one based on your setup):
//   - Android Emulator: 'http://10.0.2.2:8080' (maps to host localhost)
//   - iOS Simulator:    'http://localhost:8080'
//   - Physical Device:  'http://YOUR_PC_IP:8080' (run 'ipconfig' to find it)

type Environment = 'local' | 'aws';

// ⚙️ CHANGE THIS VALUE TO SWITCH ENVIRONMENTS
const CURRENT_ENV: Environment = 'local';

const URLS: Record<Environment, string> = {
    local: 'http://10.0.2.2:8080',        // Android emulator → host localhost (works on any PC)
    aws: 'http://13.60.208.216:8080',     // AWS EC2 Public IP
};

export const API_URL = URLS[CURRENT_ENV];

// Helper to check current environment
export const isLocalEnv = (): boolean => (CURRENT_ENV as Environment) === 'local';
export const isAwsEnv = (): boolean => (CURRENT_ENV as Environment) === 'aws';
