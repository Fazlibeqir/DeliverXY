<template>
    <Page>
        <GridLayout rows="*,auto,*" class="bg-gray-100">

            <!-- Center card -->
            <ScrollView row="1">
                <StackLayout class="bg-white p-6 mx-6 rounded-lg">

                    <Label text="Create Account" class="text-2xl font-bold text-center mb-2" />
                    <Label text="Join DeliverXY" class="text-gray-500 text-center mb-4" />

                    <TextField v-model="firstName" hint="First name" class="input mb-3" />
                    <TextField v-model="lastName" hint="Last name" class="input mb-3" />
                    <TextField v-model="username" hint="Username" class="input mb-3" />
                    <TextField v-model="email" hint="Email" keyboardType="email" class="input mb-3" />
                    <TextField v-model="phoneNumber" hint="Phone number" keyboardType="phone" class="input mb-3" />
                    <Label text="Account type" class="text-gray-600 mb-2" />

                    <GridLayout columns="*,*" class="mb-4">
                        <Button col="0" text="Client" :class="role === 'CLIENT' ? 'btn-primary' : 'btn-outline'"
                            @tap="role = 'CLIENT'" />
                        <Button col="1" text="Agent" :class="role === 'AGENT' ? 'btn-primary' : 'btn-outline'"
                            @tap="role = 'AGENT'" />
                    </GridLayout>

                    <TextField v-model="password" hint="Password" secure class="input mb-3" />

                    <Label v-if="error" :text="error" class="text-red-500 text-center mb-2" />

                    <Button :text="loading ? 'Creating account…' : 'Register'" :isEnabled="!loading" class="btn-primary"
                        @tap="submit" />

                    <Button text="Already have an account? Login" class="text-blue-500 text-center mt-3"
                        @tap="goToLogin" />

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