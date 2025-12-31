import { Frame } from "@nativescript/core";

export function goToAuth() {
  Frame.topmost()?.navigate({
    moduleName: "screens/auth/Login",
    clearHistory: true,
  });
}

export function goToClientTabs() {
  Frame.topmost()?.navigate({
    moduleName: "navigation/ClientTabs",
    clearHistory: true,
  });
}

export function goToAgentTabs() {
  Frame.topmost()?.navigate({
    moduleName: "navigation/AgentTabs",
    clearHistory: true,
  });
}
