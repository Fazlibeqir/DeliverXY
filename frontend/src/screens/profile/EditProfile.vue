<template>
  <Page>
    <ActionBar title="Edit Profile" />

    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <Label text="Update your profile information" class="section-subheader mb-4" />

        <StackLayout class="mb-3">
          <Label text="First Name" class="section-subheader mb-1" />
          <TextField v-model="firstName" hint="Enter your first name" class="input" />
        </StackLayout>

        <StackLayout class="mb-3">
          <Label text="Last Name" class="section-subheader mb-1" />
          <TextField v-model="lastName" hint="Enter your last name" class="input" />
        </StackLayout>

        <StackLayout class="mb-4">
          <Label text="Phone Number" class="section-subheader mb-1" />
          <TextField v-model="phoneNumber" hint="Enter your phone number" keyboardType="phone" class="input" />
        </StackLayout>

        <Button :isEnabled="!loading" text="Save Changes" class="btn-primary" @tap="save" />
        <StackLayout v-if="loading" class="loading-container">
          <ActivityIndicator busy="true" />
          <Label text="Saving..." class="loading-text" />
        </StackLayout>
      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { alert, Frame } from "@nativescript/core";
import * as UserService from "@/services/user.service";

const firstName = ref("");
const lastName = ref("");
const phoneNumber = ref("");
const loading = ref(false);

onMounted(async () => {
  try {
    loading.value = true;
    const me = await UserService.getMe();
    firstName.value = me.firstName || "";
    lastName.value = me.lastName || "";
    phoneNumber.value = me.phoneNumber || "";
  } catch {
    alert("Failed to load profile");
  } finally {
    loading.value = false;
  }
});

async function save() {
  try {
    loading.value = true;
    await UserService.updateMe({
      firstName: firstName.value,
      lastName: lastName.value,
      phoneNumber: phoneNumber.value,
    });
    alert("Profile updated");
    Frame.topmost().goBack();
  } catch (e: any) {
    alert(e?.message || "Failed to update profile");
  } finally {
    loading.value = false;
  }
}
</script>