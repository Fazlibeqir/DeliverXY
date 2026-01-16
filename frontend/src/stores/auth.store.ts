import { reactive } from "vue";
import * as AuthService from "../services/auth.service";
import { logger } from "../utils/logger";

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
        try {
            const user = await AuthService.login(identifier, password);
            logger.debug("Login response user:", JSON.stringify(user, null, 2));
            
            if (!user) {
                logger.error("Login returned null user");
                throw new Error("Login failed: No user data received");
            }
            
            this.setUser(user);
            logger.debug("Auth store after login - user:", this.user, "role:", this.role);
            
            if (!this.user || !this.role) {
                logger.error("setUser failed - user or role is null", { user: this.user, role: this.role });
                throw new Error("Failed to set user data");
            }
        } catch (error: any) {
            logger.error("Auth store login error:", error);
            throw error;
        }
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
        logger.debug("setUser called with:", JSON.stringify(user, null, 2));
        if (!user) {
            logger.error("Invalid user payload - user is null/undefined");
            this.clear();
            return;
        }
        
        // Check for role in different possible locations
        const role = user.role || user.userRole || user.roleType;
        if (!role) {
            logger.error("Invalid user payload - missing role field", user);
            this.clear();
            return;
        }
        
        // Normalize role: "Agent" -> "AGENT", "Client" -> "CLIENT"
        const normalizedRole = String(role).toUpperCase();
        this.user = user;
        this.role = normalizedRole as "CLIENT" | "AGENT";
        logger.debug("setUser completed - user:", this.user?.id, "role:", this.role);
    },

    clear() {
        this.user = null;
        this.role = null;
    },
});
