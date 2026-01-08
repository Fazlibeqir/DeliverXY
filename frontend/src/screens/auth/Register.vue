<template>
    <Page>
        <ActionBar title="Create Account" />
        <GridLayout rows="*" class="bg-gray-100">
            <ScrollView row="0">
                <StackLayout class="card-elevated mx-4 my-4">

                    <StackLayout class="mb-6">
                        <Label text="Join DeliverXY" class="text-2xl font-bold text-center mb-2 text-primary" />
                        <Label text="Create your account to get started" class="text-sm text-center text-secondary" />
                    </StackLayout>

                    <StackLayout class="mb-3">
                        <Label text="First Name" class="section-subheader mb-1" />
                        <TextField v-model="firstName" hint="Enter your first name" class="input" />
                    </StackLayout>

                    <StackLayout class="mb-3">
                        <Label text="Last Name" class="section-subheader mb-1" />
                        <TextField v-model="lastName" hint="Enter your last name" class="input" />
                    </StackLayout>

                    <StackLayout class="mb-3">
                        <Label text="Username" class="section-subheader mb-1" />
                        <TextField v-model="username" hint="Choose a username" class="input" />
                    </StackLayout>

                    <StackLayout class="mb-3">
                        <Label text="Email" class="section-subheader mb-1" />
                        <TextField v-model="email" hint="Enter your email" keyboardType="email" class="input" />
                    </StackLayout>

                    <StackLayout class="mb-3">
                        <Label text="Phone Number" class="section-subheader mb-1" />
                        <TextField v-model="phoneNumber" hint="Enter your phone number" keyboardType="phone" class="input" />
                    </StackLayout>

                    <StackLayout class="mb-4">
                        <Label text="Account Type" class="section-subheader mb-2" />
                        <GridLayout columns="*,*" class="mb-2">
                            <Button col="0" text="Client" :class="role === 'CLIENT' ? 'btn-primary' : 'btn-outline'"
                                @tap="role = 'CLIENT'" />
                            <Button col="1" text="Agent" :class="role === 'AGENT' ? 'btn-primary' : 'btn-outline'"
                                @tap="role = 'AGENT'" />
                        </GridLayout>
                        <Label :text="role === 'CLIENT' ? 'Order deliveries' : 'Deliver packages'" class="text-xs text-secondary text-center" />
                    </StackLayout>

                    <StackLayout class="mb-4">
                        <Label text="Password" class="section-subheader mb-1" />
                        <TextField v-model="password" hint="Create a secure password" secure class="input" />
                    </StackLayout>

                    <Label v-if="error" :text="error" class="text-danger text-center mb-3 p-2" />

                    <Button :text="loading ? 'Creating account…' : 'CREATE ACCOUNT'" :isEnabled="!loading" class="btn-primary mb-4"
                        @tap="submit" />

                    <StackLayout class="divider" />

                    <StackLayout class="text-center mt-4 mb-2">
                        <Label text="Already have an account?" class="text-secondary mb-3" />
                        <Button text="SIGN IN" class="btn-secondary-action" @tap="goToLogin" />
                    </StackLayout>

                </StackLayout>
            </ScrollView>

        </GridLayout>
    </Page>
</template>

<script setup lang="ts">
import { ref, getCurrentInstance } from "vue";
import { authStore } from "../../stores/auth.store";
import { Frame } from "@nativescript/core";
import Login from "./Login.vue";
import ClientTabs from "~/navigation/ClientTabs.vue";
import AgentTabs from "~/navigation/AgentTabs.vue";

const firstName = ref("");
const lastName = ref("");
const username = ref("");
const email = ref("");
const phoneNumber = ref("");
const password = ref("");
const role = ref<"CLIENT" | "AGENT">("CLIENT");

const instance = getCurrentInstance();
const navigateTo = instance!.proxy!.$navigateTo;

const loading = ref(false);
const error = ref("");


async function submit() {
    error.value = "";
    loading.value = true;

    try {
        await authStore.register({
            firstName: firstName.value,
            lastName: lastName.value,
            username: username.value,
            email: email.value,
            phoneNumber: phoneNumber.value,
            password: password.value,
            role: role.value,
        });
    } catch (e: any) {
        error.value = "Registration failed. Check your data.";
    } finally {
        loading.value = false;
    }
}
function goToLogin() {
    navigateTo(Login, { clearHistory: true });
}
</script>