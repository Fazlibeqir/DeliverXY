import { createApp } from "nativescript-vue";
import { createPinia } from "pinia";
import App from "./App.vue";

// Register Mapbox component
import { registerElement } from "nativescript-vue";
registerElement("Mapbox", () => require("@nativescript-community/ui-mapbox").MapboxView);

const app = createApp(App);
app.use(createPinia());
app.start();
