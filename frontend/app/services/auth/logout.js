import { logout } from "~/services/auth/auth.api";
import Login from "~/components/Login.vue";

export async function doLogout(vueInstance) {
    try {
        await logout();  // backend logout
    } catch (_) {}

    vueInstance.$navigateTo(Login, {
        clearHistory: true
    });
}
