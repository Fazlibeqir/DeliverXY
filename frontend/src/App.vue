<template>
    <Frame>
        <component :is="rootComponent" />
    </Frame>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { authStore } from "./stores/auth.store";

import Login from "./screens/auth/Login.vue";
import ClientTabs from "./navigation/ClientTabs.vue";
import AgentTabs from "./navigation/AgentTabs.vue";

import { watchEffect } from "vue";

watchEffect(() => {
    console.log("ROOT AUTH STATE", {
        loading: authStore.loading,
        user: authStore.user,
        role: authStore.role,
    });
});


onMounted(async () => {
    await authStore.bootstrap();
});

const rootComponent = computed(() => {
    if (authStore.loading) return Login; // or Splash later

    if (!authStore.user) return Login;

    if (authStore.role === "CLIENT") return ClientTabs;
    if (authStore.role === "AGENT") return AgentTabs;

    return Login;
});
</script>