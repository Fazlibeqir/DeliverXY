<template>
  <ScrollView>
    <StackLayout class="p-4">

      <Label text="Profile" class="text-xl font-bold mb-3" />

      <!-- USER INFO -->
      <Label
        :text="`Name: ${userName}`"
        class="mb-1"
      />
      <Label
        :text="`Role: ${authStore.user?.role}`"
        class="mb-3 text-gray-500"
      />

      <!-- KYC STATUS (AGENT ONLY) -->
      <StackLayout v-if="isAgent" class="mb-4">
        <Label
          :text="`KYC Status: ${kycLabel}`"
          :class="kycClass"
          class="font-bold"
        />

        <Label
          v-if="kycStore.status === 'REJECTED'"
          :text="`Reason: ${kycStore.rejectionReason}`"
          class="text-red-500 mt-1"
        />

        <Button
          v-if="needsKYC"
          text="Complete KYC"
          class="btn-primary mt-3"
          @tap="goToKYC"
        />
      </StackLayout>

      <!-- LOGOUT -->
      <Button
        text="Logout"
        class="btn-danger"
        @tap="logout"
      />

    </StackLayout>
  </ScrollView>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { authStore } from "../stores/auth.store";
import { useKYCStore } from "../stores/kyc.store";
import KYCUpload from "./agent/KYCUpload.vue";
import { getCurrentInstance } from "vue";

const instance = getCurrentInstance();
const navigateTo = instance!.proxy!.$navigateTo;

const kycStore = useKYCStore();

onMounted(() => {
  if (authStore.user?.role?.toUpperCase() === "AGENT") {
    kycStore.load();
  }
});

const userName = computed(() =>
  authStore.user
    ? `${authStore.user.firstName} ${authStore.user.lastName}`
    : ""
);

const isAgent = computed(() =>
  authStore.user?.role?.toUpperCase() === "AGENT"
);

const needsKYC = computed(() =>
  isAgent.value &&
  kycStore.status !== "APPROVED"
);

const kycLabel = computed(() => {
  switch (kycStore.status) {
    case "APPROVED":
      return "Approved";
    case "REJECTED":
      return "Rejected";
    case "PENDING":
      return "Pending";
    default:
      return "Not submitted";
  }
});

const kycClass = computed(() => {
  switch (kycStore.status) {
    case "APPROVED":
      return "text-green-600";
    case "REJECTED":
      return "text-red-600";
    default:
      return "text-orange-500";
  }
});

function goToKYC() {
  navigateTo(KYCUpload);
}

function logout() {
  authStore.logout();
}
</script>
