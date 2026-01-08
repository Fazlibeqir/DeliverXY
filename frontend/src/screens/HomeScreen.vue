<template>
  <GridLayout rows="*">
    <WebView 
      ref="wv" 
      row="0" 
      src="~/assets/map.html" 
      @loadFinished="onLoaded" 
      @consoleMessage="onConsoleMessage" />
  </GridLayout>
</template>

<script setup lang="ts">
import { alert, isAndroid, isIOS } from "@nativescript/core";
import { ref, onMounted } from "vue";
import {
  enableLocationRequest,
  getCurrentLocation,
  watchLocation,
  isEnabled,
} from "@nativescript/geolocation";
import * as DeliveryService from "../services/deliveries.service";
import * as LocationService from "../services/location.service";
import { useDeliveriesStore } from "~/stores/useDeliveryStore";


const store = useDeliveriesStore();

const wv = ref<any>(null);
const webReady = ref(false);
const deliveries = ref<any[]>([]);
const activeDelivery = ref<any | null>(null);

const lastLoc = ref<{ lat: number; lng: number } | null>(null);
const assigning = ref(false);
const assignedIds = new Set<number>();

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
        console.error("NS_BRIDGE_ERROR");
      }
    };
  `);
}

async function refreshNearby(lat: number, lng: number) {
  if (activeDelivery.value) return;
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

  const webview = wv.value?.nativeView;
  if (isAndroid && webview?.android) {
    const WebChromeClientClass = (android.webkit.WebChromeClient as any).extend({
      onConsoleMessage(consoleMessage: any) {
        const message = consoleMessage.message();
        onConsoleMessage({ android: { message } });
        return true;
      }
    });
    webview.android.setWebChromeClient(
      new (WebChromeClientClass as any)());

    const s = webview.android.getSettings();
    s.setBuiltInZoomControls(false);
    s.setDisplayZoomControls(false);
    s.setSupportZoom(true);
    s.setUseWideViewPort(false);
    s.setLoadWithOverviewMode(true);
    webview.android.setOnTouchListener(new android.view.View.OnTouchListener({
      onTouch: function(view, event) {
        return false;
      }
    }));
  }

  setTimeout(() => {
    injectBridge();
    safeRunJS(`window.setDeliveries(${js(deliveries.value)});`);
    if (lastLoc.value) {
      safeRunJS(`window.setUserLocation(${lastLoc.value.lat}, ${lastLoc.value.lng});`);
    }
    if (activeDelivery.value) {
      safeRunJS(`window.clearDeliveries();`);
    }
  }, 300);
}

async function loadActiveDelivery() {
  const active = await DeliveryService.getActiveDelivery();
  if (!active) {
    activeDelivery.value = null;
    safeRunJS(`window.clearRoute();`);
    return false;
  }

  activeDelivery.value = active;

  safeRunJS(`
    window.clearDeliveries();
    window.drawRoute(
      ${active.pickupLatitude},
      ${active.pickupLongitude},
      ${active.dropoffLatitude},
      ${active.dropoffLongitude}
    );
  `);

  return true;
}
(globalThis as any).__homeTabActivated = async () => {
  const hasActive = await loadActiveDelivery();
  if (hasActive) return;
  if (!lastLoc.value) return;

  await refreshNearby(lastLoc.value.lat, lastLoc.value.lng);
  };


(globalThis as any).__deliveryStatusChanged = (d: any) => {
  if (d.status === "CANCELLED" || d.status === "DELIVERED") {
    activeDelivery.value = null;
    safeRunJS(`window.clearRoute();`);
    if (lastLoc.value) {
      refreshNearby(lastLoc.value.lat, lastLoc.value.lng);
    }
    return;
  }

  activeDelivery.value = d;

  safeRunJS(`
    window.clearRoute();
    window.drawRoute(
      ${d.status === "ASSIGNED" ? d.pickupLatitude : d.dropoffLatitude},
      ${d.status === "ASSIGNED" ? d.pickupLongitude : d.dropoffLongitude},
      ${d.dropoffLatitude},
      ${d.dropoffLongitude}
    );
  `);
};



async function onConsoleMessage(e: any) {
  let msg = e?.android?.message ?? e?.message;
  if (!msg) return;

  if (!msg.trim().startsWith('{') && !msg.trim().startsWith('"')) {
    return;
  }

  if (msg.startsWith('"') && msg.endsWith('"')) {
    msg = msg.slice(1, -1);
  }

  try {
    const parsed = JSON.parse(msg);

    if (parsed.__nsEvent && parsed.payload?.type === "accepted") {
      const deliveryId = parsed.payload.data.id;

      if (assigning.value || assignedIds.has(deliveryId)) return;
      assigning.value = true;
      assignedIds.add(deliveryId);

      try {
        const assigned = await DeliveryService.assignDelivery(deliveryId);

        activeDelivery.value = assigned;

        alert("Delivery assigned. Navigate to pickup.");

        deliveries.value = [];
        safeRunJS(`window.clearDeliveries();`);

        if (lastLoc.value) {
          safeRunJS(`
             window.drawRoute(
              ${parsed.payload.data.lat},
              ${parsed.payload.data.lng},
              ${assigned.dropoffLatitude},
              ${assigned.dropoffLongitude}
             );  
          `); 
        }else {
          safeRunJS(`
            window.drawRoute(
              ${assigned.pickupLatitude},
              ${assigned.pickupLongitude},
              ${assigned.dropoffLatitude},
              ${assigned.dropoffLongitude}
            );
          `);
        }

        store.loadAssigned(true);
      } catch (err) {
        await alert(String((err as any)?.message || "Assign failed"));
        assignedIds.delete(deliveryId);
      } finally {
        assigning.value = false;
      }
    }
  } catch (err) {}
}

async function pushLocation(lat: number, lng: number) {
  try {
    await LocationService.updateDriverLocation(lat, lng);
  } catch (e) {}
}

onMounted(async () => {
  const enabled = await isEnabled();
  if (!enabled) {
    try {
      await enableLocationRequest(true, true);
    } catch (e) {
      alert("Location permission is required to use this app.");
    }
  }

  await loadActiveDelivery();

  if (await isEnabled()) {
    try {
      const loc = await getCurrentLocation({ timeout: 20000 });
      lastLoc.value = { lat: loc.latitude, lng: loc.longitude };

      safeRunJS(`window.setUserLocation(${loc.latitude}, ${loc.longitude});`);
      pushLocation(loc.latitude, loc.longitude);
      if (!activeDelivery.value) {
        await refreshNearby(loc.latitude, loc.longitude);
      }
    } catch (e) {}
  }

  let lastNearbyFetch = 0;

  if (await isEnabled()) {
  watchLocation(
    (l) => {
      lastLoc.value = { lat: l.latitude, lng: l.longitude };

      safeRunJS(`window.setUserLocation(${l.latitude}, ${l.longitude});`);
      pushLocation(l.latitude, l.longitude);
      if (activeDelivery.value) {
        const target =
          activeDelivery.value.status === "ASSIGNED"
            ? {
              lat: activeDelivery.value.pickupLatitude,
              lng: activeDelivery.value.pickupLongitude,
            }
            : {
              lat: activeDelivery.value.dropoffLatitude,
              lng: activeDelivery.value.dropoffLongitude,
            };
        safeRunJS(`
        window.drawRoute(
          ${l.latitude},
          ${l.longitude},
          ${target.lat},
          ${target.lng}
        );
      `);
      } else {
        const now = Date.now();
        if (now - lastNearbyFetch > 8000) {
          lastNearbyFetch = now;
          refreshNearby(l.latitude, l.longitude);
        }
      }
    },
    () => {},
    { minimumUpdateTime: 2000, desiredAccuracy: 3 }
  );
  }
});
</script>