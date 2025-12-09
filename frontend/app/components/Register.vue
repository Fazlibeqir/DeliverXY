<template>
    <Page>
        <ActionBar title="Register" />

        <ScrollView>
            <StackLayout class="form">

                <Label text="Create Account" class="title" />

                <TextField v-model="username" hint="Username" class="input" />
                <TextField v-model="email" hint="Email" keyboardType="email" class="input" />
                <TextField v-model="password" hint="Password" secure="true" class="input" />
                <TextField v-model="firstName" hint="First Name" class="input" />
                <TextField v-model="lastName" hint="Last Name" class="input" />
                <TextField v-model="phoneNumber" hint="Phone Number" class="input" />

                <!-- ROLE SELECTOR -->
                <Label text="Account Type:" class="label" />
                <Picker :items="roles" v-model="selectedRole" class="picker" />

                <!-- ERROR -->
                <Label v-if="error" :text="error" class="error" textWrap="true" />

                <!-- REGISTER BUTTON -->
                <Button text="Register" class="btn btn-primary" :isEnabled="!loading" @tap="submit" />

                <!-- NAVIGATION -->
                <Label text="Already have an account?" class="link" @tap="goLogin" />

            </StackLayout>
        </ScrollView>
    </Page>
</template>

<script>
import { register } from "~/services/auth/auth.api";
import AgentHome from "./home/AgentHome.vue";
import ClientHome from "./home/ClientHome.vue";
import Login from "./Login.vue";

export default {
    data() {
        return {
            username: "",
            email: "",
            password: "",
            firstName: "",
            lastName: "",
            phoneNumber: "",
            selectedRole: 0,
            roles: ["CLIENT", "AGENT"],

            loading: false,
            error: null
        };
    },

    methods: {
        async submit() {
            this.error = null;
            this.loading = true;

            try {
                const payload = {
                    username: this.username,
                    email: this.email,
                    password: this.password,
                    role: this.roles[this.selectedRole],
                    firstName: this.firstName,
                    lastName: this.lastName,
                    phoneNumber: this.phoneNumber
                };

                const user = await register(payload);

                // Navigate based on role
                if (user.role === "Agent" || user.role === "AGENT") {
                    this.$navigateTo(AgentHome, { clearHistory: true });
                } else {
                    this.$navigateTo(ClientHome, { clearHistory: true });
                }

            } catch (e) {
                this.error = e.message || "Registration failed";
            } finally {
                this.loading = false;
            }
        },

        goLogin() {
            this.$navigateTo(Login, { clearHistory: true });
        }
    }
};
</script>

<style scoped>
.form {
    padding: 20;
}

.title {
    font-size: 24;
    margin-bottom: 20;
    font-weight: bold;
    text-align: center;
}

.input {
    margin-bottom: 12;
    background: #fff;
    padding: 10;
    border-radius: 6;
}

.label {
    margin-top: 10;
}

.picker {
    margin-bottom: 20;
}

.btn-primary {
    background-color: #007AFF;
    color: white;
}

.error {
    color: red;
    margin-bottom: 10;
}

.link {
    margin-top: 15;
    text-align: center;
    color: #007AFF;
}
</style>