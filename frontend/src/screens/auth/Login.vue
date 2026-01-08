<template>
  <Page>
    <GridLayout rows="*,auto,*" class="bg-gray-100">
      <StackLayout row="1" class="card-elevated mx-6">
        <StackLayout class="mb-6">
          <Label text="DeliverXY" class="text-3xl font-bold text-center mb-2 text-primary" />
          <Label text="Welcome back!" class="text-lg text-center mb-1 text-secondary" />
          <Label text="Sign in to continue" class="text-sm text-center text-secondary" />
        </StackLayout>

        <StackLayout class="mb-4">
          <Label text="Email or Username" class="section-subheader mb-1" />
          <TextField v-model="identifier" hint="Enter your email or username" class="input" />
        </StackLayout>

        <StackLayout class="mb-4">
          <Label text="Password" class="section-subheader mb-1" />
          <TextField v-model="password" hint="Enter your password" secure class="input" />
        </StackLayout>

        <Label v-if="error" :text="error" class="text-danger text-center mb-3 p-2" />

        <Button :text="loading ? 'Logging in…' : 'SIGN IN'" :isEnabled="!loading" class="btn-primary mb-4" @tap="submit" />
        
        <StackLayout class="divider" />
        
        <StackLayout class="text-center mt-4 mb-2">
          <Label text="Don't have an account?" class="text-secondary mb-3" />
          <Button text="CREATE ACCOUNT" class="btn-secondary-action" @tap="goToRegister" />
        </StackLayout>

      </StackLayout>

    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { getCurrentInstance, ref } from "vue";
import { authStore } from "../../stores/auth.store";
import Register from "./Register.vue";

const identifier = ref("");
const password = ref("");
const loading = ref(false);
const error = ref("");

const instance = getCurrentInstance();
const navigateTo = instance!.proxy!.$navigateTo;
async function submit() {
  error.value = "";
  loading.value = true;

  try {
    await authStore.login(identifier.value, password.value);
  } catch (e: any) {
    error.value = "Invalid credentials";
  } finally {
    loading.value = false;
  }
}
function goToRegister() {
  navigateTo(Register);
}
</script>
