<template>
  <Page>
    <GridLayout rows="*,auto,*" class="bg-gray-100">

      <!-- Center card -->
      <StackLayout
        row="1"
        class="bg-white p-6 mx-6 rounded-lg shadow"
      >
        <Label
          text="DeliverXY"
          class="text-2xl font-bold text-center mb-2"
        />
        <Label
          text="Sign in to continue"
          class="text-gray-500 text-center mb-4"
        />

        <TextField
          v-model="identifier"
          hint="Email or Username"
          class="input mb-3"
        />

        <TextField
          v-model="password"
          hint="Password"
          secure
          class="input mb-3"
        />

        <Label
          v-if="error"
          :text="error"
          class="text-red-500 text-center mb-2"
        />

        <Button
          :text="loading ? 'Logging in…' : 'Login'"
          :isEnabled="!loading"
          class="btn-primary"
          @tap="submit"
        />
      </StackLayout>

    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { authStore } from "../../stores/auth.store";

const identifier = ref("");
const password = ref("");
const loading = ref(false);
const error = ref("");

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
</script>
