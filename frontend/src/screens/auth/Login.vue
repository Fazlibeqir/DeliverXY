<template>
  <Page>
    <GridLayout rows="*,auto,*" class="bg-gray-100">
      <StackLayout row="1" class="card-elevated mx-6 p-6">
        <!-- Header -->
        <StackLayout class="mb-8">
          <Label text="📦 DeliverXY" class="text-4xl font-bold text-center mb-3 text-primary" />
          <Label text="Welcome back!" class="text-xl text-center mb-2 text-secondary font-semibold" />
          <Label text="Sign in to continue" class="text-sm text-center text-secondary" />
        </StackLayout>

        <!-- Login Form -->
        <StackLayout class="mb-4">
          <Label text="Email or Username *" class="section-subheader mb-2" />
          <TextField 
            v-model="identifier" 
            hint="Enter your email or username" 
            class="input" 
            :isEnabled="!loading"
          />
        </StackLayout>

        <StackLayout class="mb-5">
          <Label text="Password *" class="section-subheader mb-2" />
          <TextField 
            v-model="password" 
            hint="Enter your password" 
            secure 
            class="input" 
            :isEnabled="!loading"
          />
        </StackLayout>

        <!-- Error Message -->
        <StackLayout v-if="error" class="card mb-4 p-3" style="background-color: #FFEBEE;">
          <Label :text="error" class="text-danger text-center text-sm" />
        </StackLayout>

        <!-- Login Button -->
        <Button 
          :text="loading ? '⏳ Logging in…' : '✓ SIGN IN'" 
          :isEnabled="isFormValid" 
          class="btn-primary mb-4" 
          @tap="submit" 
        />
        
        <StackLayout class="divider mb-4" />
        
        <!-- Register Link -->
        <StackLayout class="text-center">
          <Label text="Don't have an account?" class="text-secondary mb-3" />
          <Button 
            text="CREATE ACCOUNT" 
            class="btn-secondary-action" 
            :isEnabled="!loading"
            @tap="goToRegister" 
          />
        </StackLayout>

      </StackLayout>

    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { getCurrentInstance, ref, computed } from "vue";
import { authStore } from "../../stores/auth.store";
import Register from "./Register.vue";

const identifier = ref("");
const password = ref("");
const loading = ref(false);
const error = ref("");

const instance = getCurrentInstance();
const navigateTo = instance!.proxy!.$navigateTo;

// Computed property to check if form is valid
const isFormValid = computed(() => {
  return !loading.value && identifier.value.trim().length > 0 && password.value.trim().length > 0;
});

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
