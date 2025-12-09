<template>
    <Page>
        <ActionBar title="DeliverXY Login" />

        <StackLayout class="form">

            <TextField v-model="identifier" hint="Email or Username" />
            <TextField v-model="password" hint="Password" secure="true" />

            <Button text="Login" @tap="submit" :isEnabled="!loading" class="btn btn-primary" />

            <Label v-if="error" :text="error" class="error" />

            <Label text="Don't have an account? Register here."
           class="link"
           @tap="goRegister"
           marginTop="30" />
        </StackLayout>
    </Page>
</template>

<script>
import { login } from "~/services/auth/auth.api";
import { getMe } from "~/services/auth/auth.api";
import AgentHome from "./home/AgentHome.vue";
import ClientHome from "./home/ClientHome.vue";
import Register from "./Register.vue";
function normalizeRole(role) {
    return role ? role.trim().toUpperCase() : null;
}

export default {
    data() {
        return {
            identifier: "",
            password: "",
            loading: false,
            error: null
        }
    },
    methods: {
        goRegister() {
            this.$navigateTo(Register);
        },

        async submit() {
            this.error = null;
            this.loading = true;

            try {
                await login(this.identifier, this.password);

                const user = await getMe();
                const role = normalizeRole(user.role);

                if (role === "AGENT") {
                    this.$navigateTo(AgentHome, { clearHistory: true });
                } else {
                    this.$navigateTo(ClientHome, { clearHistory: true });
                }

            } catch (e) {
                this.error = e.message || "Login failed";
            } finally {
                this.loading = false;
            }
        }
    }
}

</script>