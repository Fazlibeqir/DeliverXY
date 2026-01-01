import { createApp } from "nativescript-vue";
import App from "./App.vue";

import { registerElement } from "nativescript-vue";
import { MapLibreView } from "@nativescript-community/ui-maplibre";

registerElement("MapLibreView", () => MapLibreView);

createApp(App).start();
