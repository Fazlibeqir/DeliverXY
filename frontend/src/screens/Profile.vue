<template>
  <GridLayout rows="*">
    <ScrollView row="0">
      <StackLayout class="p-4">
      <Label text="Profile" class="section-header mb-4" />

      <StackLayout class="card-elevated mb-4">
        <StackLayout class="mb-3">
          <Label text="Account Information" class="section-subheader mb-2" />
          <Label :text="userName" class="text-xl font-bold mb-1" />
          <Label :text="authStore.user?.role === 'CLIENT' ? 'Client Account' : 'Delivery Agent'" class="text-secondary" />
        </StackLayout>
        <StackLayout class="divider" />
        <StackLayout class="mt-2">
          <Label :text="`Email: ${authStore.user?.email || 'N/A'}`" class="text-sm text-secondary mb-1" />
          <Label :text="`Phone: ${authStore.user?.phoneNumber || 'N/A'}`" class="text-sm text-secondary" />
        </StackLayout>
      </StackLayout>

      <StackLayout v-if="isAgent" class="card-elevated mb-4">
        <Label text="Verification Status" class="section-subheader mb-3" />
        <StackLayout class="mb-3">
          <Label :text="kycLabel" :class="kycClass" class="font-bold text-lg mb-2" />
          <Label v-if="kycStore.status === 'PENDING'"
            text="Our team is reviewing your documents. This usually takes 24-48 hours."
            class="text-secondary text-sm" />

          <Label v-if="kycStore.status === 'APPROVED'" text="✓ You are verified and can accept deliveries."
            class="text-success text-sm" />

          <Label v-if="kycStore.status === 'REJECTED'" :text="`Reason: ${kycStore.rejectionReason}`"
            class="text-danger text-sm mt-1" />
        </StackLayout>

        <Button v-if="needsKYC" text="Complete KYC" class="btn-primary-compact" @tap="goToKYC" />
      </StackLayout>

      <StackLayout class="mb-4">
        <Label text="Account Actions" class="section-subheader mb-2" />
        <Button text="Edit Profile" class="btn-outline mb-2" @tap="goToEditProfile" />
        <Button v-if="isAgent" text="My Vehicles" class="btn-outline mb-2" @tap="goToVehicles" />
        <Button v-if="isAgent" text="Earnings & Payouts" class="btn-outline mb-2" @tap="goToEarnings" />
        <Button v-if="isAgent" text="My Ratings" class="btn-outline mb-2" @tap="goToMyRatings" />
      </StackLayout>

      <Button text="Logout" class="btn-danger" @tap="logout" />

    </StackLayout>
    </ScrollView>
  </GridLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from "vue";
import { authStore } from "../stores/auth.store";
import { useKYCStore } from "../stores/kyc.store";
import KYCUpload from "./agent/KYCUpload.vue";
import { getCurrentInstance } from "vue";
import EditProfile from "./profile/EditProfile.vue";
import Vehicles from "./agent/Vehicles.vue";
import Earnings from "./agent/Earnings.vue";
import MyRatings from "./agent/MyRatings.vue";

const instance = getCurrentInstance();
const navigateTo = instance!.proxy!.$navigateTo;

const kycStore = useKYCStore();

const isAgent = computed(() =>
  authStore.user?.role?.toUpperCase() === "AGENT"
);

onMounted(async () => {
  if (isAgent.value) await kycStore.load();
  (globalThis as any).__profileTabActivated = async () => {
    if (isAgent.value) {
    await kycStore.load();
  }
  };
});
onUnmounted(() => {
  delete (globalThis as any).__profileTabActivated;
});
const userName = computed(() =>
  authStore.user
    ? `${authStore.user.firstName} ${authStore.user.lastName}`
    : ""
);



const needsKYC = computed(() =>
  isAgent.value &&
  (!kycStore.status || kycStore.status === "REJECTED")
);


const kycLabel = computed(() => {
  if (kycStore.loading) return "Loading…";
  switch (kycStore.status) {
    case "APPROVED": return "Verified ✔";
    case "REJECTED": return "Rejected ❌";
    case "PENDING": return "Under review ⏳";
    default: return "Not submitted";
  }
});

const kycClass = computed(() => {
  if (kycStore.loading) return "text-gray-500";
  switch (kycStore.status) {
    case "APPROVED": return "text-green-600";
    case "REJECTED": return "text-red-600";
    default: return "text-orange-500";
  }
});

function goToKYC() {
  navigateTo(KYCUpload, {
    props: {
      onDone: async () => { await kycStore.load(); }
    }
  });
}

function logout() {
  authStore.logout();
}

function goToEditProfile() {
  navigateTo(EditProfile);
}
function goToVehicles() {
  navigateTo(Vehicles);
}
function goToEarnings() {
  navigateTo(Earnings);
}
function goToMyRatings() {
  navigateTo(MyRatings);
}
</script>
