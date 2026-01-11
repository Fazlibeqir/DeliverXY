# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).

## Admin API configuration

Create a `.env` file in the project root:

```bash
VITE_API_URL=http://localhost:8080
```

This app uses these backend endpoints:

- **Auth**: `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`
- **Current user**: `/api/users/me`
- **Admin Dashboard**: `/api/admin/dashboard`
- **Users**: `/api/admin/users`, `/api/admin/users/{id}/block`, `/api/admin/users/{id}/unblock`
- **Deliveries**: `/api/admin/deliveries`, `/api/admin/deliveries/{deliveryId}/assign`
- **Tracking**: `/api/tracking/{deliveryId}`
- **Earnings**: `/api/admin/earnings`
- **Payouts**: `/api/admin/payouts`
- **Promo Codes**: `/api/promo-codes/all`, `/api/promo-codes`, `/api/promo-codes/{id}/deactivate`

## Features

- Admin authentication with token refresh
- User management (view, block/unblock)
- Delivery management and tracking
- Interactive delivery map with real-time simulation
- Earnings and payouts overview
- Promo code management
- Dashboard with statistics and charts

## Development

```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Map Simulation

The delivery map includes a simulation mode that:
- Generates 7 concurrent simulated deliveries across North Macedonia
- Uses real road routing via OSRM
- Animates agents moving along routes
- Automatically replaces completed deliveries
- Shows pickup, dropoff, and agent locations