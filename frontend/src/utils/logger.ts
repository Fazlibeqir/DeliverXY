/**
 * Logger utility for production-safe logging
 * In production, only errors are logged
 */

const isDevelopment = process.env.NODE_ENV === 'development' || __DEV__;

// Check if console methods exist (console.debug may not exist in NativeScript)
const hasDebug = typeof console !== 'undefined' && typeof console.debug === 'function';
const hasInfo = typeof console !== 'undefined' && typeof console.info === 'function';

export const logger = {
  log: (...args: any[]) => {
    if (isDevelopment) {
      console.log(...args);
    }
  },
  
  error: (...args: any[]) => {
    // Always log errors, even in production
    console.error(...args);
  },
  
  warn: (...args: any[]) => {
    if (isDevelopment) {
      if (typeof console.warn === 'function') {
        console.warn(...args);
      } else {
        console.log(...args);
      }
    }
  },
  
  debug: (...args: any[]) => {
    if (isDevelopment) {
      // Use console.log if console.debug doesn't exist (NativeScript compatibility)
      if (hasDebug) {
        console.debug(...args);
      } else {
        console.log(...args);
      }
    }
  },
  
  info: (...args: any[]) => {
    if (isDevelopment) {
      // Use console.log if console.info doesn't exist
      if (hasInfo) {
        console.info(...args);
      } else {
        console.log(...args);
      }
    }
  }
};
