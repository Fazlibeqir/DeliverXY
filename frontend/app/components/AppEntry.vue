<template>
    <Page>
        <Label text="Loading..." />
    </Page>
</template>

<script>
import Login from "./Login.vue";
import AgentHome from "./home/AgentHome.vue";
import ClientHome from "./home/ClientHome.vue";

import { getAccessToken, clearToken } from "~/services/core/auth-store";
import { apiFetch } from "~/services/core/api-fetch";

function normalizeRole(role) {
    return role ? role.trim().toUpperCase() : null;
}
export default {
    async mounted() {
        const token = getAccessToken();

        // No token → go to login
        if (!token) {
            this.$navigateTo(Login, { clearHistory: true });
            return;
        }

        try {
            // Validate token and get user info
            const user = await apiFetch("/auth/me");
            const role = normalizeRole(user.role);

            // Navigate based on role
            if (role === "AGENT") {
                this.$navigateTo(AgentHome, { clearHistory: true });
            } else {
                this.$navigateTo(ClientHome, { clearHistory: true });
            }

        } catch (e) {
            // Invalid token → logout → go to login
            clearToken();
            this.$navigateTo(Login, { clearHistory: true });
        }
    }
};
</script>
