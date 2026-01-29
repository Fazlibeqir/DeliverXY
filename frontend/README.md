# DeliverXY Mobile Application

Cross-platform mobile application built with NativeScript-Vue 3 for iOS and Android.

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Development](#development)
- [Building for Production](#building-for-production)
- [Troubleshooting](#troubleshooting)

---

## Overview

DeliverXY mobile app provides two distinct user experiences:

- **Client Interface**: Create delivery requests, track status, manage wallet, rate services
- **Agent Interface**: View available deliveries on map, accept deliveries, manage vehicles, track earnings, complete KYC

---

## Technology Stack

- **Framework**: NativeScript-Vue 3
- **Language**: TypeScript
- **UI Framework**: Vue 3 (Composition API)
- **State Management**: Pinia
- **Maps**: Mapbox SDK
- **Styling**: Tailwind CSS
- **Storage**: NativeScript Secure Storage
- **Location**: NativeScript Geolocation
- **Camera**: NativeScript Camera

---

## Prerequisites

### Required Software

- **Node.js**: 18+ (LTS recommended)
- **npm**: 9+ (comes with Node.js)
- **Java JDK**: 17+ (for Android development)
- **Android Studio**: Latest version (for Android development)
- **Xcode**: Latest version (for iOS development, macOS only)
- **CocoaPods**: Latest version (for iOS development, macOS only)

### Platform-Specific Requirements

#### Android
- Android SDK (via Android Studio)
- Android SDK Platform 33+
- Android Build Tools

#### iOS (macOS only)
- Xcode 14+
- CocoaPods
- iOS Simulator or physical device

---

## Setup & Installation

### 1. Install NativeScript CLI

```bash
npm install -g @nativescript/cli
```

### 2. Install Dependencies

```bash
cd frontend
npm install
```

### 3. Configure API Endpoint

Edit `src/config.ts`:

```typescript
export const API_URL = 'http://your-backend-url:8080';
```

**Note**: For Android emulator, use `http://10.0.2.2:8080` instead of `localhost`.

### 4. Configure Mapbox (Optional)

If using Mapbox for maps:

1. Get Mapbox access token from [mapbox.com](https://www.mapbox.com)
2. Configure in `nativescript.config.ts` or environment variables

---

## Configuration

### API Configuration

The API URL is configured in `src/config.ts`:

```typescript
// Use your backend URL (e.g. local or deployed server)
export const API_URL = 'http://your-backend-host:8080';
```

For Android emulator, use `http://10.0.2.2:8080` instead of `localhost`.

### Environment Variables

Create `.env` file (if needed):

```bash
API_URL=http://localhost:8080
MAPBOX_ACCESS_TOKEN=your-mapbox-token
```

### NativeScript Configuration

Edit `nativescript.config.ts` for:
- App ID
- App name
- Version
- Build configuration

---

## Running the Application

### Development Mode

#### Android

```bash
# Start Android emulator or connect device
ns run android
```

#### iOS (macOS only)

```bash
# Start iOS simulator or connect device
ns run ios
```

### Live Reload

```bash
# Android with live reload
ns run android --watch

# iOS with live reload
ns run ios --watch
```

### Debug Mode

```bash
# Android debug
ns debug android

# iOS debug
ns debug ios
```

---

## Project Structure

```
src/
├── app.ts                    # Application entry point
├── App.vue                   # Root component with routing
├── app.css                   # Global styles (Tailwind)
├── config.ts                 # API configuration
├── screens/                  # Feature screens
│   ├── auth/                # Authentication screens
│   │   ├── Login.vue
│   │   └── Register.vue
│   ├── client/              # Client features
│   │   ├── ClientHome.vue
│   │   ├── ClientDeliveries.vue
│   │   └── DeliveryDetailsPage.vue
│   ├── agent/               # Agent features
│   │   ├── KYCUpload.vue
│   │   ├── Vehicles.vue
│   │   └── Earnings.vue
│   ├── profile/             # Profile management
│   ├── wallet/             # Wallet features
│   └── ratings/            # Rating screens
├── services/                # API services
│   ├── api.ts              # Axios instance
│   ├── auth.service.ts
│   ├── deliveries.service.ts
│   ├── wallet.service.ts
│   ├── vehicles.service.ts
│   ├── kyc.service.ts
│   ├── earnings.service.ts
│   ├── ratings.service.ts
│   ├── user.service.ts
│   ├── geocoding.service.ts
│   └── location.service.ts
├── stores/                  # Pinia stores
│   ├── auth.store.ts
│   ├── kyc.store.ts
│   └── useDeliveryStore.ts
├── components/              # Reusable components
│   ├── payments/
│   └── SkeletonLoader.vue
├── navigation/              # Tab navigation
│   ├── AgentTabs.vue
│   └── ClientTabs.vue
└── utils/                   # Utility functions
    ├── debounce.ts
    ├── errorHandler.ts
    └── logger.ts
```

---

## Development

### Code Style

- Use Vue 3 Composition API with `<script setup>`
- TypeScript for type safety
- Pinia for state management
- Service layer pattern for API calls

### Adding New Features

1. Create screen component in `src/screens/`
2. Create service in `src/services/` if needed
3. Add to navigation if required
4. Update stores if state management needed

### State Management

#### Auth Store

```typescript
import { authStore } from './stores/auth.store';

// Check authentication
if (authStore.isAuthenticated) {
  // User is logged in
}

// Get user role
const role = authStore.role; // 'CLIENT' | 'AGENT'
```

#### Delivery Store

```typescript
import { useDeliveryStore } from './stores/useDeliveryStore';

const deliveryStore = useDeliveryStore();
await deliveryStore.loadDeliveries();
```

### API Service Pattern

```typescript
// services/example.service.ts
import { api } from './api';

export const exampleService = {
  async getData(): Promise<Data[]> {
    const response = await api.get('/api/endpoint');
    return response.data.data;
  }
};
```

---

## Building for Production

### Android

#### Debug Build

```bash
ns build android
```

#### Release Build

```bash
ns build android --release --key-store-path <path-to-keystore> --key-store-password <password> --key-store-alias <alias> --key-store-alias-password <alias-password>
```

### iOS (macOS only)

#### Debug Build

```bash
ns build ios
```

#### Release Build

```bash
ns build ios --release
```

Then archive and distribute via Xcode.

### Build Output

- **Android**: `platforms/android/app/build/outputs/apk/`
- **iOS**: `platforms/ios/build/`

---

## Key Features

### Authentication

- JWT-based authentication
- Token refresh mechanism
- Secure token storage
- Role-based navigation

### Delivery Management

- Create delivery requests
- Real-time status tracking
- Map-based delivery viewing (agents)
- Delivery history

### Wallet System

- View balance
- Transaction history
- Payment processing

### Location Services

- GPS tracking
- Map integration (Mapbox)
- Geocoding (OpenStreetMap Nominatim)

### KYC Verification

- Document upload
- Status tracking

### Vehicle Management

- Register vehicles
- View vehicle list
- Update vehicle info

---

## Troubleshooting

### Android Issues

#### Build Errors

```bash
# Clean build
ns clean android
ns run android
```

#### Emulator Connection

- Use `http://10.0.2.2:8080` instead of `localhost:8080`
- Ensure emulator is running before `ns run android`

#### Gradle Issues

```bash
cd platforms/android
./gradlew clean
```

### iOS Issues (macOS only)

#### CocoaPods Issues

```bash
cd platforms/ios
pod install
```

#### Build Errors

```bash
# Clean build
ns clean ios
ns run ios
```

### General Issues

#### Module Not Found

```bash
# Reinstall dependencies
rm -rf node_modules
npm install
```

#### TypeScript Errors

```bash
# Check TypeScript version
npm list typescript
```

#### API Connection Issues

- Verify API URL in `src/config.ts`
- Check backend is running
- Verify network connectivity
- Check CORS settings on backend

---

## Performance Tips

1. **Lazy Loading**: Load data on demand
2. **Image Optimization**: Compress images before upload
3. **API Caching**: Cache frequently accessed data
4. **Debouncing**: Debounce search inputs
5. **List Virtualization**: Use virtual scrolling for long lists

---

## Security Considerations

- Tokens stored in secure storage
- HTTPS for API calls (in production)
- Input validation
- Error handling without exposing sensitive data

---