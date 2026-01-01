<template>
  <GridLayout rows="*,auto">
    <WebView ref="wv" row="0" src="~/assets/map.html" @loadFinished="onLoaded" @consoleMessage="onConsoleMessage" />

   

  </GridLayout>
</template>



<script setup lang="ts">
import { isAndroid, isIOS } from "@nativescript/core";
import { ref, onMounted } from "vue";
import {
  enableLocationRequest,
  getCurrentLocation,
  watchLocation,
} from "@nativescript/geolocation";


const wv = ref<any>(null);
const webReady = ref(false);


// Example deliveries (later from backend)
const deliveries = [
  {
    id: "d1",
    title: "Delivery #1",
    description: "Pickup near center",
    lat: 41.9981,
    lng: 21.4254,
  },
  {
    id: "d2",
    title: "Delivery #2",
    description: "Express delivery",
    lat: 42.0,
    lng: 21.44,
  },
];




function js(obj: any) {
  return JSON.stringify(obj)
    .replace(/\\/g, "\\\\")
    .replace(/`/g, "\\`");
}

function runJS(code: string) {
  const webview = wv.value?.nativeView;
  if (!webview) return;

  if (isAndroid && webview.android) {
    webview.android.evaluateJavascript(code, null);
  } else if (isIOS && webview.ios) {
    webview.ios.evaluateJavaScriptCompletionHandler(code, null);
  }
}
// ✅ SAFE WRAPPER (THIS WAS MISSING)
function safeRunJS(code: string) {
  if (!webReady.value) return;

  runJS(`
    if (window.nsReady) {
      ${code}
    }
  `);
}
function injectBridge() {
  safeRunJS(`
    window.nsEmit = function(payload) {
      try {
        console.log(JSON.stringify({
          __nsEvent: true,
          payload: payload
        }));
      } catch (e) {
        console.log("NS_BRIDGE_ERROR");
      }
    };
  `);
}



function onLoaded() {
  webReady.value = true;
  setTimeout(() => {
    injectBridge();
    safeRunJS(`window.setDeliveries(${js(deliveries)});`);
  }, 300);
}


function onConsoleMessage(e: any) {
  let msg = e?.android?.message ?? e?.message;
  if (!msg) return;

  if (msg.startsWith('"') && msg.endsWith('"')) {
    msg = msg.slice(1, -1);
  }

  try {
    const parsed = JSON.parse(msg);
    if (parsed.type === "accepted") {
      console.log("Delivery accepted:", parsed.data);
      // call backend, change tab, etc.
    }
  } catch {}
}










onMounted(async () => {
  await enableLocationRequest(true);

  const loc = await getCurrentLocation({ timeout: 20000 });
  safeRunJS(`window.setUserLocation(${loc.latitude}, ${loc.longitude});`);

  watchLocation(
    (l) =>
    safeRunJS(`window.setUserLocation(${l.latitude}, ${l.longitude});`),
    (e) => console.log("watch error", e),
    { minimumUpdateTime: 2000, desiredAccuracy: 3 }
  );
});
</script>
