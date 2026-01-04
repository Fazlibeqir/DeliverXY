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
import * as DeliveryService from "../services/deliveries.service";
import * as LocationService from "../services/location.service";
import { useDeliveriesStore } from "~/stores/useDeliveryStore";


const store = useDeliveriesStore();

const wv = ref<any>(null);
const webReady = ref(false);
const deliveries = ref<any[]>([]);



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

async function refreshNearby(lat: number, lng: number) {
  const res = await DeliveryService.getNearbyDeliveries(lat, lng);

  deliveries.value = res.map((d: any) => ({
    id: d.id,
    title: d.title,
    description: d.pickupAddress,
    lat: d.pickupLatitude,
    lng: d.pickupLongitude,
  }));

  safeRunJS(`window.setDeliveries(${js(deliveries.value)});`);
}




function onLoaded() {
  webReady.value = true;

  // 🔥 disable Android WebView zoom UI (bottom bar)
  const webview = wv.value?.nativeView;
  if (isAndroid && webview?.android) {
    const s = webview.android.getSettings();
    s.setBuiltInZoomControls(false);   // removes bottom zoom bar
    s.setDisplayZoomControls(false);   // extra safety
    s.setSupportZoom(false);           // optional: disables zoom gestures too

    s.setUseWideViewPort(false);
    s.setLoadWithOverviewMode(true);
  }

  setTimeout(() => {
    injectBridge();
    safeRunJS(`window.setDeliveries(${js(deliveries.value)});`);
  }, 300);
}


async function onConsoleMessage(e: any) {
  let msg = e?.android?.message ?? e?.message;
  if (!msg) return;

  if (msg.startsWith('"') && msg.endsWith('"')) {
    msg = msg.slice(1, -1);
  }

  try {
    const parsed = JSON.parse(msg);

    if (parsed.type === "accepted") {
      const deliveryId = parsed.data.id;

      try {
        await DeliveryService.assignDelivery(deliveryId);

        // remove from map
        deliveries.value = deliveries.value.filter(d => d.id !== deliveryId);
        safeRunJS(`window.setDeliveries(${js(deliveries.value)});`);

        store.loadAssigned();

        console.log("Delivery assigned:", deliveryId);
      } catch (e) {
        console.log("Failed to assign delivery", e);
      }
    }
  } catch {}
}

async function pushLocation(lat: number, lng: number) {
  try {
    await LocationService.updateDriverLocation(lat, lng);
  } catch (e) {
    console.log("Location update failed", e);
  }
}

onMounted(async () => {
  await enableLocationRequest(true);

  const loc = await getCurrentLocation({ timeout: 20000 });

  safeRunJS(`window.setUserLocation(${loc.latitude}, ${loc.longitude});`);
  await pushLocation(loc.latitude, loc.longitude);
  await refreshNearby(loc.latitude, loc.longitude);

  watchLocation(
    (l) =>{
      safeRunJS(`window.setUserLocation(${l.latitude}, ${l.longitude});`);
      pushLocation(l.latitude, l.longitude);
      refreshNearby(l.latitude, l.longitude);
    },
    (e) => console.log("watch error", e),
    { minimumUpdateTime: 2000, desiredAccuracy: 3 }
  );
});
</script>
