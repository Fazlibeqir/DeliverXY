<template>
  <WebView ref="webViewRef" :src="htmlSrc" @loadFinished="onLoadFinished" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import { WebView } from "@nativescript/core";
import {
  injectNsEmitBridge,
  installWebViewBridge,
  runWebViewJavaScript,
} from "../utils/webview-map";

const props = defineProps<{
  mode: "agent" | "client";
}>();

const emit = defineEmits<{
  ready: [];
  message: [payload: { type: string; data?: unknown }];
}>();

const webViewRef = ref<WebView | null>(null);
const htmlSrc =
  props.mode === "client" ? "~/assets/client-map.html" : "~/assets/map.html";

function getWebView(): WebView | null {
  return (webViewRef.value as any)?.nativeView ?? webViewRef.value;
}

function onLoadFinished() {
  const wv = getWebView();
  if (!wv) return;
  installWebViewBridge(wv, (msg) => emit("message", msg));
  injectNsEmitBridge(wv);
  emit("ready");
}

function run(script: string) {
  const wv = getWebView();
  if (wv) runWebViewJavaScript(wv, script);
}

defineExpose({
  setUserLocation(lat: number, lng: number) {
    run(`window.setUserLocation(${lat}, ${lng});`);
  },
  setDeliveries(items: Array<{ id: number; lat: number; lng: number; title: string; description: string }>) {
    run(`window.setDeliveries(${JSON.stringify(items)});`);
  },
  clearDeliveries() {
    run("window.clearDeliveries();");
  },
  drawRoute(fromLat: number, fromLng: number, toLat: number, toLng: number) {
    run(`window.drawRoute(${fromLat}, ${fromLng}, ${toLat}, ${toLng});`);
  },
  drawFullRoute(
    agentLat: number,
    agentLng: number,
    pickupLat: number,
    pickupLng: number,
    dropoffLat: number,
    dropoffLng: number
  ) {
    run(
      `window.drawFullRoute(${agentLat}, ${agentLng}, ${pickupLat}, ${pickupLng}, ${dropoffLat}, ${dropoffLng});`
    );
  },
  clearRoute() {
    run("window.clearRoute();");
  },
  setPickupMarker(lat: number, lng: number) {
    run(`window.setPickupMarker(${lat}, ${lng});`);
  },
  setDropoffMarker(lat: number, lng: number) {
    run(`window.setDropoffMarker(${lat}, ${lng});`);
  },
  setMapCenter(lat: number, lng: number, zoom = 15) {
    run(`window.setMapCenter(${lat}, ${lng}, ${zoom});`);
  },
});
</script>
