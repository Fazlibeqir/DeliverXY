<template>
  <GridLayout rows="*,auto">

    <!-- MAP -->
    <MapLibreView row="0" height="100%" width="100%" @mapReady="onMapReady" />

    <!-- BOTTOM CARD -->
    <StackLayout row="1" class="bg-white p-4">
      <Label text="Nearby deliveries" class="font-bold mb-2" />

      <Label v-if="!selected" text="Tap a delivery on the map" class="text-gray-500" />

      <StackLayout v-else>
        <Label :text="selected.title" class="font-bold" />
        <Label :text="selected.pickupAddress" class="text-gray-500 mb-2" />
        <Button text="Accept Delivery" />
      </StackLayout>
    </StackLayout>

  </GridLayout>
</template>


<script setup lang="ts">
import { ref, onMounted } from "vue";
import {
  enableLocationRequest,
  getCurrentLocation,
} from "@nativescript/geolocation";

import { Http } from "@nativescript/core";
import type { MapEventData } from "@nativescript-community/ui-maplibre";

const selected = ref<any>(null);

// Example deliveries (later comes from backend)
const deliveries = [
  {
    id: "d1",
    title: "Delivery #1",
    description: "Pickup near center",
    lng: 21.4254,
    lat: 41.9981,
  },
  {
    id: "d2",
    title: "Delivery #2",
    description: "Express delivery",
    lng: 21.44,
    lat: 42.0,
  },
];
onMounted(async () => {
  await enableLocationRequest(true);
});

function onMapReady(event: any) {
  const mapView = event.object; // ✅ THIS is correct
  const map = mapView.getMap(); // get native map instance

  map.setStyle(
    new URL("https://demotiles.maplibre.org/style.json")
  );

  map.on("load", async () => {
    let loc;
    // User location
    try {
      loc = await getCurrentLocation({
        desiredAccuracy: 3,
        timeout: 20000,
      });
      console.log("Location:", loc);
    } catch {
      // fallback: Skopje
      loc = { latitude: 41.9981, longitude: 21.4254 };
    }

    // Center map
    map.easeTo({
      center: [loc.longitude, loc.latitude],
      zoom: 14,
    });
    map.addLayer({
  id: "test",
  type: "background",
  paint: {
    "background-color": "red",
  },
});


    // Draw user location
    addUserLocation(map, loc);

    // Draw deliveries
    addDeliveries(map);

    // Handle taps on deliveries
    map.on("click", "deliveries-layer", (e: any) => {
      selected.value = e.features[0].properties;
    });
  });
}
function addUserLocation(map: any, loc: any) {
  map.addSource("user-location", {
    type: "geojson",
    data: {
      type: "FeatureCollection",
      features: [
        {
          type: "Feature",
          geometry: {
            type: "Point",
            coordinates: [loc.longitude, loc.latitude],
          },
        },
      ],
    },
  });
  map.addLayer({
    id: "user-location-layer",
    type: "circle",
    source: "user-location",
    paint: {
      "circle-radius": 10,
      "circle-color": "#007aff",
      "circle-stroke-width": 3,
      "circle-stroke-color": "#ffffff",
    },
  });
}

function addDeliveries(map: any) {
  map.addSource("deliveries", {
    type: "geojson",
    data: {
      type: "FeatureCollection",
      features: deliveries.map((d) => ({
        type: "Feature",
        geometry: {
          type: "Point",
          coordinates: [d.lng, d.lat],
        },
        properties: {
          title: d.title,
          description: d.description,
        },
      })),
    },
  });

  map.addLayer({
    id: "deliveries-layer",
    type: "circle",
    source: "deliveries",
    paint: {
      "circle-radius": 8,
      "circle-color": "#ff3b30",
      "circle-stroke-width": 2,
      "circle-stroke-color": "#ffffff",
    },
  });
}
</script>
