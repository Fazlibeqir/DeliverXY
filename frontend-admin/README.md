# DeliverXY Admin Panel

Vue.js 3 admin dashboard for managing the DeliverXY platform.

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
- [Deployment](#deployment)

---

## Overview

The DeliverXY Admin Panel is a web-based dashboard that provides administrators with:

- **Dashboard**: Platform statistics and analytics
- **User Management**: View and manage users
- **KYC Approval**: Review and approve/reject KYC documents
- **Delivery Management**: View and manage all deliveries
- **Map View**: Real-time delivery tracking on map
- **Earnings Overview**: Platform earnings analytics
- **Payout Management**: Process driver payouts
- **Promo Code Management**: Create and manage promotional codes
- **Pricing & Commission**: Configure base prices, rates, multipliers, and platform commission

---

## Technology Stack

- **Framework**: Vue.js 3
- **Build Tool**: Vite
- **Router**: Vue Router 4
- **State Management**: Pinia
- **HTTP Client**: Axios
- **Styling**: Tailwind CSS
- **Charts**: Chart.js with vue-chartjs
- **Maps**: Leaflet
- **Language**: JavaScript

---

## Prerequisites

- **Node.js**: 18+ (LTS recommended)
- **npm**: 9+ (comes with Node.js)
- **Modern Browser**: Chrome, Firefox, Safari, Edge (latest versions)

---

## Setup & Installation

### 1. Install Dependencies

```bash
cd frontend-admin
npm install
```

### 2. Configure API Endpoint

Create `.env` file or set environment variable:

```bash
VITE_API_URL=http://localhost:8080
```

For production, set to your backend URL:
```bash
VITE_API_URL=http://your-backend-host:8080
```

### 3. Start Development Server

```bash
npm run dev
```

The application will be available at: `http://localhost:5173`

---

## Configuration

### Environment Variables

Create `.env` file in `frontend-admin/` directory:

```bash
# API Base URL
VITE_API_URL=http://localhost:8080

# For production (your backend URL)
# VITE_API_URL=http://your-backend-host:8080
```

### API Configuration

The API URL is configured in `src/config.js`:

```javascript
export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
```

### Build-time Configuration

Environment variables prefixed with `VITE_` are injected at build time. Make sure to rebuild after changing `.env`:

```bash
npm run build
```

---

## Running the Application

### Development Mode

```bash
npm run dev
```

Access at: `http://localhost:5173`

### Preview Production Build

```bash
npm run build
npm run preview
```

### Production Build

```bash
npm run build
```

Output will be in `dist/` directory.

---

## Project Structure

```
src/
├── App.vue                    # Root component
├── main.js                    # Application entry point
├── config.js                  # API configuration
├── style.css                  # Global styles
├── components/                # Page components
│   ├── Dashboard.vue         # Dashboard with statistics
│   ├── DashboardCard.vue      # Reusable card component
│   ├── Deliveries.vue         # Delivery management
│   ├── DeliveryMap.vue        # Map view
│   ├── DeliveriesOverTimeChart.vue  # Chart component
│   ├── DeliveryStatsChart.vue # Statistics chart
│   ├── Earnings.vue           # Earnings overview
│   ├── JsonViewer.vue         # JSON viewer component
│   ├── KYCApproval.vue        # KYC approval interface
│   ├── Login.vue              # Admin login
│   ├── Payouts.vue            # Payout management
│   ├── PricingConfig.vue      # Pricing & commission config
│   ├── PromoCodes.vue         # Promo code management
│   └── Users.vue              # User management
├── router/
│   └── index.js               # Vue Router configuration
├── services/                  # API services
│   ├── axios.js               # Axios instance
│   ├── http.js                 # HTTP service wrapper
│   ├── apiResponse.js         # API response handling
│   └── authTokens.js          # Token management
└── stores/
    └── auth.js                # Pinia auth store
```

---

## Development

### Code Style

- Vue 3 Composition API with `<script setup>`
- Tailwind CSS for styling
- Pinia for state management
- Axios for API calls

### Adding New Features

1. Create component in `src/components/`
2. Add route in `src/router/index.js`
3. Create service method if needed
4. Update navigation in `App.vue`

### State Management

#### Auth Store

```javascript
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()

// Check authentication
if (auth.isAuthenticated) {
  // User is authenticated
}

// Check admin role
if (auth.isAdmin) {
  // User is admin
}
```

### API Service Pattern

```javascript
// services/example.js
import { http } from './http'

export const exampleService = {
  async getData() {
    const response = await http.get('/api/endpoint')
    return response.data
  }
}
```

### Router Configuration

Routes are protected with navigation guards:

```javascript
{
  path: '/protected',
  component: ProtectedComponent,
  meta: { 
    requiresAuth: true, 
    requiresAdmin: true 
  }
}
```

---

## Key Features

### Dashboard

- Platform statistics
- Delivery metrics
- Earnings overview
- Interactive charts (Chart.js)

### User Management

- View all users
- Filter and search
- View user details
- KYC status

### KYC Approval

- View KYC documents
- Approve/reject KYC
- View document details

### Delivery Management

- View all deliveries
- Filter by status
- Search deliveries
- View delivery details

### Map View

- Real-time delivery locations
- Interactive map (Leaflet)
- Delivery markers
- Route visualization

### Earnings & Payouts

- Earnings overview
- Payout management
- Statistics and charts

### Promo Codes

- Create promo codes
- View usage statistics
- Manage active codes

### Pricing & Commission

- View and edit pricing configs
- Set base fare, per km/minute rates, minimum fare
- Set multipliers (surge, night, weekend, peak)
- Set platform commission (cut) percentage

---

## Building for Production

### Build Command

```bash
npm run build
```

### Build Output

The build output will be in `dist/` directory, ready to be served by a web server.

### Docker Build

The project includes a Dockerfile for containerized deployment:

```bash
docker build -t deliverxy-frontend-admin .
```

### Nginx Configuration

The project includes `nginx.conf` for production serving:

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## Deployment

### Docker Deployment

```bash
# Build image
docker build -t deliverxy-frontend-admin .

# Run container
docker run -p 80:80 deliverxy-frontend-admin
```

### Static Hosting

1. Build the project: `npm run build`
2. Upload `dist/` contents to your web server
3. Configure server to serve `index.html` for all routes (SPA routing)

### Environment Variables in Production

Set `VITE_API_URL` at build time:

```bash
VITE_API_URL=http://your-backend-url:8080 npm run build
```

### Kubernetes Deployment

See `kubernetes/frontend-deployment.yaml` for Kubernetes configuration.

---

## API Integration

### Authentication

The admin panel uses JWT authentication:

1. Login via `/login`
2. Token stored in Pinia store
3. Token included in API requests via Axios interceptor
4. Auto-logout on token expiration

### API Endpoints Used

- `POST /api/auth/login` - Admin login
- `GET /api/admin/dashboard` - Dashboard statistics
- `GET /api/admin/users` - User list
- `GET /api/admin/deliveries` - Delivery list
- `GET /api/admin/earnings` - Earnings data
- `GET /api/admin/payouts` - Payout list
- `PUT /api/admin/kyc/{userId}/approve` - Approve KYC
- `PUT /api/admin/kyc/{userId}/reject` - Reject KYC

---

## Troubleshooting

### API Connection Issues

- Verify `VITE_API_URL` is set correctly
- Check backend is running
- Verify CORS settings on backend
- Check browser console for errors

### Build Issues

```bash
# Clean and reinstall
rm -rf node_modules dist
npm install
npm run build
```

### Routing Issues

Ensure your web server is configured to serve `index.html` for all routes (SPA routing).

### Authentication Issues

- Check token storage
- Verify token expiration
- Check backend authentication endpoint

---

## Development Tips

1. **Hot Reload**: Vite provides instant hot module replacement
2. **DevTools**: Use Vue DevTools for debugging
3. **Network Tab**: Monitor API calls in browser DevTools
4. **Console**: Check browser console for errors

---

## Security Considerations

- Admin-only access (backend enforces)
- JWT token storage
- HTTPS in production
- Input validation
- XSS prevention (Vue.js built-in)

