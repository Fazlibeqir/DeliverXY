import { createApp } from "nativescript-vue";
import { createPinia  } from "pinia";
import App from "./App.vue";

import { registerElement } from "nativescript-vue";
import { MapLibreView } from "@nativescript-community/ui-maplibre";

registerElement("MapLibreView", () => MapLibreView);

const app = createApp(App);
app.use(createPinia());
app.start();
