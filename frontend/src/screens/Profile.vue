<template>
  <StackLayout class="p-4">
    <Label text="Profile" class="text-xl font-bold mb-3" />

    <Label text="Agent: Beqir Fazli" class="mb-2" />
    <Label text="KYC: Pending" class="text-orange-500 mb-4" />

    <Button text="Logout" class="btn-danger" @tap="logout" />
  </StackLayout>
</template>

<script setup lang="ts">
import { authStore } from "../stores/auth.store";

function logout() {
  authStore.logout();
}
</script>
