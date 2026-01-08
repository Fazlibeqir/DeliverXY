import { reactive } from "vue";
import * as AuthService from "../services/auth.service";

export const authStore = reactive({
    user: null as any,
    role: null as "CLIENT" | "AGENT" | null,
    loading: true,

    async bootstrap() {
        try {
            const user = await AuthService.refresh();
            this.setUser(user);
        } catch {
            this.clear();
        } finally {
            this.loading = false;
        }
    },

    async login(identifier: string, password: string) {
        const user = await AuthService.login(identifier, password);
        this.setUser(user);
    },

    async register(payload: any) {
        const user = await AuthService.register(payload);
        this.setUser(user);
    },

    async logout() {
        await AuthService.logout();
        this.clear();
        this.loading = false;
    },

    setUser(user: any) {
        if (!user || !user.role) {
            console.error("Invalid user payload", user);
            this.clear();
            return;
        }
        const normalizedRole = String(user.role).toUpperCase();
        this.user = user;
        this.role = normalizedRole as "CLIENT" | "AGENT";
    },

    clear() {
        this.user = null;
        this.role = null;
    },
});
