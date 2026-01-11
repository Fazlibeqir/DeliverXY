# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).

## Admin API configuration

Create a `.env` file in the project root:

```bash
VITE_API_URL=http://localhost:8080
```

This app uses these backend endpoints:

- Auth: `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`
- Current user: `/api/users/me`
- Admin: `/api/admin/dashboard`, `/api/admin/users`, `/api/admin/users/{id}/block`, `/api/admin/users/{id}/unblock`, `/api/admin/deliveries`, `/api/admin/deliveries/{deliveryId}/assign`