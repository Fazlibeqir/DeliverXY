import { NativeScriptConfig } from '@nativescript/core';

export default {
  id: 'org.nativescript.frontend',
  appPath: 'src',
  appResourcesPath: 'App_Resources',
  android: {
    v8Flags: '--expose_gc',
    markingMode: 'none',
    permissions: [
      'INTERNET',
      'ACCESS_FINE_LOCATION',
      'ACCESS_COARSE_LOCATION',
    ],
  }
} as NativeScriptConfig;