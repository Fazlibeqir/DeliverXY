import "@triniwiz/nativescript-stripe";
import { createApp } from "nativescript-vue";
import { createPinia  } from "pinia";
import App from "./App.vue";

import { registerElement } from "nativescript-vue";
import { CreditCardView } from '@triniwiz/nativescript-stripe';

registerElement("CreditCardView", () => CreditCardView);

const app = createApp(App);
app.use(createPinia());
app.start();
