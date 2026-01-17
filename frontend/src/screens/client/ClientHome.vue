<template>
  <GridLayout rows="auto, *, auto">
    <!-- Minimal Header -->
    <StackLayout row="0" class="p-3 bg-white" style="border-bottom-width: 1; border-bottom-color: #e0e0e0;">
      <Label text="📍 Select Locations" class="text-base font-bold" />
      <Label :text="instructionText" class="text-xs text-secondary mt-1" />
    </StackLayout>

    <!-- Map - Full Screen -->
    <GridLayout row="1">
      <Mapbox
        :key="mapKey"
        ref="mapbox"
        :accessToken="mapboxToken"
        :mapStyle="currentMapStyle"
        :latitude="mapCenterLat"
        :longitude="mapCenterLng"
        :zoomLevel="13"
        :showUserLocation="true"
        @mapReady="onMapReady"
        @mapTap="onMapTap"
        @mapError="onMapError"
      />
      
      <!-- Map Loading Skeleton Overlay -->
      <SkeletonLoader 
        v-show="mapLoading" 
        type="map" 
        containerStyle="position: absolute; width: 100%; height: 100%; background-color: rgba(245, 245, 245, 0.95);"
      />
      
      <!-- Floating Buttons -->
      <StackLayout 
        horizontalAlignment="right"
        verticalAlignment="bottom"
        margin="15"
      >
        <!-- My Location Button -->
        <Button 
          text="📍"
          class="btn-primary"
          style="width: 50; height: 50; border-radius: 25; background-color: #000000; color: #ffffff; font-size: 20; padding: 0;"
          @tap="centerOnMyLocation"
        />
      </StackLayout>
    </GridLayout>

    <!-- Bottom Bar -->
    <StackLayout row="2" class="p-4 bg-white" style="border-top-width: 1; border-top-color: #e0e0e0;">
      <!-- Pickup Section -->
      <StackLayout v-if="!pickupLat || !pickupLng" class="mb-3">
        <Label text="🚚 Pickup Location" class="text-sm font-bold mb-2" />
        <GridLayout columns="*, auto" columnsGap="8">
          <Button col="0" text="📍 Use My Location" 
                  class="btn-outline" 
                  @tap="useMyLocation" />
          <Button col="1" text="🔍 Search" 
                  class="btn-outline" 
                  @tap="onSearch" />
        </GridLayout>
      </StackLayout>
      
      <!-- Pickup Selected -->
      <StackLayout v-else-if="pickupLat && pickupLng && !dropoffLat && !dropoffLng" class="mb-3">
        <Label text="🚚 Pickup Selected" class="text-sm font-bold mb-1" />
        <Label :text="pickupAddress || 'Pickup location'" 
               class="text-xs text-secondary mb-2" 
               textWrap="true" />
        <Button text="🔍 Search Dropoff" 
                class="btn-outline" 
                @tap="onSearch" />
      </StackLayout>
      
      <!-- Dropoff Selected from Search (show above buttons) -->
      <StackLayout v-if="dropoffLat && dropoffLng && dropoffAddress" class="mb-3">
        <Label text="🎯 Dropoff Selected" class="text-sm font-bold mb-1" />
        <Label :text="dropoffAddress" 
               class="text-xs text-secondary mb-2" 
               textWrap="true" />
      </StackLayout>
      
      <!-- Action Buttons -->
      <GridLayout v-if="pickupLat && pickupLng && dropoffLat && dropoffLng" 
                  columns="*, *" 
                  columnsGap="8">
        <Button col="0" text="🔍 Change" 
                class="btn-outline" 
                @tap="onSearch" />
        <Button col="1" text="Next →" 
                class="btn-primary" 
                @tap="onNext" />
      </GridLayout>
    </StackLayout>
  </GridLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, getCurrentInstance } from "vue";
import { getCurrentLocation, enableLocationRequest, isEnabled } from "@nativescript/geolocation";
import { alert, Application } from "@nativescript/core";
import { $showModal } from "nativescript-vue";
import AddressSearchModal from "./AddressSearchModal.vue";
import DeliveryDetailsPage from "./DeliveryDetailsPage.vue";
import { logger } from "../../utils/logger";
import SkeletonLoader from "../../components/SkeletonLoader.vue";

const mapboxToken = (process.env.MAPBOX_ACCESS_TOKEN as string) || "";

const mapbox = ref<any>(null);
const mapInstance = ref<any>(null);
const pickupMarker = ref<any>(null);
const dropoffMarker = ref<any>(null);

const mapCenterLat = ref(41.9981);
const mapCenterLng = ref(21.4254);
const userLocation = ref<{ lat: number; lng: number } | null>(null);
const hasCenteredInitially = ref(false);
const mapLoading = ref(true);
const mapKey = ref(0); // Key to force remount when tab is activated
const currentMapStyle = ref("streets");

const pickupLat = ref<number | null>(null);
const pickupLng = ref<number | null>(null);
const pickupAddress = ref<string>("");

const dropoffLat = ref<number | null>(null);
const dropoffLng = ref<number | null>(null);
const dropoffAddress = ref<string>("");

const instructionText = computed(() => {
  if (!pickupLat.value || !pickupLng.value) {
    return "Select pickup location or use your current location";
  }
  if (!dropoffLat.value || !dropoffLng.value) {
    return "Tap map for dropoff • Tap marker to remove";
  }
  return "Both locations selected • Tap markers to remove";
});

const instance = getCurrentInstance();
const navigateTo = instance!.proxy!.$navigateTo;

function onMapReady(args: any) {
  mapInstance.value = args.map;
  mapLoading.value = false;
  logger.log("Client map ready");
  
  if (args.object) {
    mapbox.value = args.object;
    logger.log("Mapbox component stored:", {
      hasNativeView: !!args.object.nativeView,
      hasMap: !!mapInstance.value
    });
    
    // Note: Attachment listener removed - we handle refresh in __homeTabActivated instead
    // to avoid conflicts and ensure proper timing
  }
  
  // Load location and zoom in when map is ready
  loadCurrentLocation();
  
  // If we already have location, zoom in on it
  if (userLocation.value && mapInstance.value) {
    setTimeout(() => {
      if (mapInstance.value && userLocation.value) {
        try {
          // Set zoom level to 15 for a closer view
          if (typeof mapInstance.value.setZoomLevel === 'function') {
            mapInstance.value.setZoomLevel({
              level: 15,
              animated: true
            });
          } else if (typeof mapInstance.value.zoomTo === 'function') {
            mapInstance.value.zoomTo(15, true);
          }
          // Center on location
          if (mapInstance.value.setCenter) {
            mapInstance.value.setCenter({
              lat: userLocation.value.lat,
              lng: userLocation.value.lng,
              animated: true
            });
          }
        } catch (e) {
          logger.error("Error zooming to location:", e);
        }
      }
    }, 300);
  }
}

function onMapError(args: any) {
  logger.error("Map error occurred:", args);
}

async function loadCurrentLocation() {
  // Get location but don't auto-center - let user pan freely
  try {
    const enabled = await isEnabled();
    if (!enabled) {
      await enableLocationRequest(false, false);
      const stillDisabled = !(await isEnabled());
      if (stillDisabled) {
        logger.warn("Location permission denied");
        return;
      }
    }
    
    const loc = await getCurrentLocation({ timeout: 20000 });
    userLocation.value = { lat: loc.latitude, lng: loc.longitude };
    
    // Only center and zoom once on initial load
    if (!hasCenteredInitially.value && mapInstance.value) {
      mapCenterLat.value = loc.latitude;
      mapCenterLng.value = loc.longitude;
      try {
        // Set zoom level to 15 for a closer view
        if (typeof mapInstance.value.setZoomLevel === 'function') {
          mapInstance.value.setZoomLevel({
            level: 15,
            animated: true
          });
        } else if (typeof mapInstance.value.zoomTo === 'function') {
          mapInstance.value.zoomTo(15, true);
        }
        // Center on location
        if (mapInstance.value.setCenter) {
          mapInstance.value.setCenter({
            lat: loc.latitude,
            lng: loc.longitude,
            animated: true
          });
        }
        hasCenteredInitially.value = true;
      } catch (e) {
        logger.error("Error centering and zooming to location:", e);
      }
    }
  } catch (e) {
    logger.error("Failed to get location:", e);
  }
}

async function centerOnMyLocation() {
  if (!userLocation.value) {
    // Try to get location if we don't have it
    try {
      const enabled = await isEnabled();
      if (!enabled) {
        await enableLocationRequest(false, false);
        const stillDisabled = !(await isEnabled());
        if (stillDisabled) {
          alert("Location permission is required. Please enable it in settings.");
          return;
        }
      }
      const loc = await getCurrentLocation({ timeout: 20000 });
      userLocation.value = { lat: loc.latitude, lng: loc.longitude };
    } catch (e) {
      alert("Failed to get your location");
      return;
    }
  }
  
  if (mapInstance.value && userLocation.value) {
    try {
      // Set zoom level to 15 for a closer view
      if (typeof mapInstance.value.setZoomLevel === 'function') {
        mapInstance.value.setZoomLevel({
          level: 15,
          animated: true
        });
      } else if (typeof mapInstance.value.zoomTo === 'function') {
        mapInstance.value.zoomTo(15, true);
      }
      // Center on location
      if (mapInstance.value.setCenter) {
        mapInstance.value.setCenter({
          lat: userLocation.value.lat,
          lng: userLocation.value.lng,
          animated: true
        });
      }
    } catch (e) {
      logger.error("Error centering and zooming to location:", e);
    }
  }
}

async function useMyLocation() {
  try {
    const enabled = await isEnabled();
    if (!enabled) {
      await enableLocationRequest(false, false);
      const stillDisabled = !(await isEnabled());
      if (stillDisabled) {
        alert("Location permission is required. Please enable it in settings.");
        return;
      }
    }
    
    const loc = await getCurrentLocation({ timeout: 20000 });
    pickupLat.value = loc.latitude;
    pickupLng.value = loc.longitude;
    
    if (mapInstance.value?.setCenter) {
      mapInstance.value.setCenter({
        lat: loc.latitude,
        lng: loc.longitude,
        animated: true
      });
    }
    
    await reverseGeocode(loc.latitude, loc.longitude, "pickup");
    updatePickupMarker(loc.latitude, loc.longitude);
  } catch (e) {
    logger.error("Failed to get location:", e);
    alert("Failed to get your location. Please tap on the map to select pickup location.");
  }
}

function onMapTap(args: any) {
  if (!mapInstance.value) {
    return;
  }
  
  // Try different event formats
  let lat: number, lng: number;
  
  if (args.location) {
    lat = args.location.latitude;
    lng = args.location.longitude;
  } else if (args.latitude && args.longitude) {
    lat = args.latitude;
    lng = args.longitude;
  } else if (args.lat && args.lng) {
    lat = args.lat;
    lng = args.lng;
  } else if (args.getLatitude && args.getLongitude) {
    // Native Android LatLng object
    lat = args.getLatitude();
    lng = args.getLongitude();
  } else {
    return;
  }
  
  if (!pickupLat.value || !pickupLng.value) {
    pickupLat.value = lat;
    pickupLng.value = lng;
    reverseGeocode(lat, lng, "pickup");
    updatePickupMarker(lat, lng);
  } else if (!dropoffLat.value || !dropoffLng.value) {
    dropoffLat.value = lat;
    dropoffLng.value = lng;
    reverseGeocode(lat, lng, "dropoff");
    updateDropoffMarker(lat, lng);
    // Center to show both locations when dropoff is selected
    if (mapInstance.value?.setCenter && pickupLat.value) {
      const centerLat = (pickupLat.value + lat) / 2;
      const centerLng = (pickupLng.value + lng) / 2;
      mapInstance.value.setCenter({
        lat: centerLat,
        lng: centerLng,
        animated: true
      });
    }
  } else {
    // Re-select dropoff - don't auto-center
    dropoffLat.value = lat;
    dropoffLng.value = lng;
    reverseGeocode(lat, lng, "dropoff");
    updateDropoffMarker(lat, lng);
  }
}

async function reverseGeocode(lat: number, lng: number, type: "pickup" | "dropoff") {
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`;
    const response = await fetch(url, {
      headers: {
        "User-Agent": "DeliverXY/1.0 (contact@deliverxy.app)",
        "Accept": "application/json"
      }
    });
    const data = await response.json();
    
    if (data?.display_name) {
      if (type === "pickup") {
        pickupAddress.value = data.display_name;
        if (pickupMarker.value && mapInstance.value?.updateMarker) {
          mapInstance.value.updateMarker(pickupMarker.value, {
            subtitle: data.display_name
          });
        }
      } else {
        dropoffAddress.value = data.display_name;
        if (dropoffMarker.value && mapInstance.value?.updateMarker) {
          mapInstance.value.updateMarker(dropoffMarker.value, {
            subtitle: data.display_name
          });
        }
      }
    }
  } catch (e) {
    logger.error("Reverse geocoding failed:", e);
  }
}

function updatePickupMarker(lat: number, lng: number) {
  if (!mapInstance.value) return;
  
  // Remove old marker if exists
  if (pickupMarker.value) {
    try {
      if (typeof mapInstance.value.removeMarkers === 'function') {
        mapInstance.value.removeMarkers([pickupMarker.value]);
      } else if (typeof mapInstance.value.removeMarker === 'function') {
        mapInstance.value.removeMarker(pickupMarker.value);
      } else if (pickupMarker.value.remove) {
        pickupMarker.value.remove();
      }
    } catch (e) {
      logger.error("Error removing old pickup marker:", e);
    }
    pickupMarker.value = null;
  }
  
  // Add new marker (only one)
  try {
    const markers = mapInstance.value.addMarkers([{
      id: "pickup_location",
      lat,
      lng,
      title: "🚚 Pickup",
      subtitle: pickupAddress.value || "Pickup location",
      onTap: () => {
        removePickupMarker();
      }
    }]);
    if (markers && markers.length > 0) {
      pickupMarker.value = markers[0];
    }
  } catch (e) {
    logger.error("Error adding pickup marker:", e);
  }
}

function removePickupMarker() {
  if (!mapInstance.value || !pickupMarker.value) return;
  
  try {
    if (typeof mapInstance.value.removeMarkers === 'function') {
      mapInstance.value.removeMarkers([pickupMarker.value]);
    } else if (typeof mapInstance.value.removeMarker === 'function') {
      mapInstance.value.removeMarker(pickupMarker.value);
    } else if (pickupMarker.value.remove) {
      pickupMarker.value.remove();
    }
    pickupMarker.value = null;
    pickupLat.value = null;
    pickupLng.value = null;
    pickupAddress.value = "";
  } catch (e) {
    logger.error("Error removing pickup marker:", e);
  }
}

function updateDropoffMarker(lat: number, lng: number) {
  if (!mapInstance.value) return;
  
  // Remove old marker if exists
  if (dropoffMarker.value) {
    try {
      if (typeof mapInstance.value.removeMarkers === 'function') {
        mapInstance.value.removeMarkers([dropoffMarker.value]);
      } else if (typeof mapInstance.value.removeMarker === 'function') {
        mapInstance.value.removeMarker(dropoffMarker.value);
      } else if (dropoffMarker.value.remove) {
        dropoffMarker.value.remove();
      }
    } catch (e) {
      logger.error("Error removing old dropoff marker:", e);
    }
    dropoffMarker.value = null;
  }
  
  // Add new marker (only one)
  try {
    const markers = mapInstance.value.addMarkers([{
      id: "dropoff_location",
      lat,
      lng,
      title: "🎯 Dropoff",
      subtitle: dropoffAddress.value || "Dropoff location",
      onTap: () => {
        removeDropoffMarker();
      }
    }]);
    if (markers && markers.length > 0) {
      dropoffMarker.value = markers[0];
    }
  } catch (e) {
    logger.error("Error adding dropoff marker:", e);
  }
}

function removeDropoffMarker() {
  if (!mapInstance.value || !dropoffMarker.value) return;
  
  try {
    if (typeof mapInstance.value.removeMarkers === 'function') {
      mapInstance.value.removeMarkers([dropoffMarker.value]);
    } else if (typeof mapInstance.value.removeMarker === 'function') {
      mapInstance.value.removeMarker(dropoffMarker.value);
    } else if (dropoffMarker.value.remove) {
      dropoffMarker.value.remove();
    }
    dropoffMarker.value = null;
    dropoffLat.value = null;
    dropoffLng.value = null;
    dropoffAddress.value = "";
  } catch (e) {
    logger.error("Error removing dropoff marker:", e);
  }
}

async function onSearch() {
  const result = await $showModal(AddressSearchModal, {
    props: {},
    fullscreen: false,
    animated: true,
  });
  
  if (result?.lat && result.lng) {
    if (!pickupLat.value || !pickupLng.value) {
      pickupLat.value = result.lat;
      pickupLng.value = result.lng;
      pickupAddress.value = result.label;
      updatePickupMarker(result.lat, result.lng);
      if (mapInstance.value?.setCenter) {
        mapInstance.value.setCenter({
          lat: result.lat,
          lng: result.lng,
          animated: true
        });
      }
    } else {
      dropoffLat.value = result.lat;
      dropoffLng.value = result.lng;
      dropoffAddress.value = result.label;
      updateDropoffMarker(result.lat, result.lng);
      if (mapInstance.value?.setCenter) {
        mapInstance.value.setCenter({
          lat: result.lat,
          lng: result.lng,
          animated: true
        });
      }
    }
  }
}

function onNext() {
  if (!pickupLat.value || !pickupLng.value || !dropoffLat.value || !dropoffLng.value) {
    alert("Please select both pickup and dropoff locations");
    return;
  }
  
  navigateTo(DeliveryDetailsPage, {
    props: {
      pickupLat: pickupLat.value,
      pickupLng: pickupLng.value,
      pickupAddress: pickupAddress.value,
      dropoffLat: dropoffLat.value,
      dropoffLng: dropoffLng.value,
      dropoffAddress: dropoffAddress.value,
    }
  });
}

// Handle app lifecycle events
let resumeHandler: any = null;

onMounted(() => {
  // Set up app resume handler - ONLY reload style on resume
  resumeHandler = Application.on(Application.resumeEvent, () => {
    if (!mapbox.value) return;
    
    // Single style reload after resume (300ms delay to let app settle)
    setTimeout(() => {
      if (mapbox.value) {
        // Force Vue to update the mapStyle prop by temporarily changing it
        const tempStyle = currentMapStyle.value;
        currentMapStyle.value = "outdoors";
        setTimeout(() => {
          currentMapStyle.value = tempStyle;
        }, 50);
      }
    }, 300);
  });
});

onUnmounted(() => {
  // Clean up event handlers
  if (resumeHandler) {
    Application.off(Application.resumeEvent, resumeHandler);
    resumeHandler = null;
  }
});

// Handle tab activation
(globalThis as any).__homeTabActivated = () => {
  logger.log("=== CLIENT HOME TAB ACTIVATED ===");
  
  // Check map state
  logger.log("Map state check:", {
    hasMapbox: !!mapbox.value,
    hasNativeView: !!(mapbox.value?.nativeView),
    currentStyle: currentMapStyle.value,
    hasMapInstance: !!mapInstance.value
  });
  
  // Check if view is attached
  let isAttached = false;
  let hasWindow = false;
  if (mapbox.value?.nativeView) {
    try {
      if (mapbox.value.nativeView.getWindow) {
        hasWindow = !!mapbox.value.nativeView.getWindow();
        isAttached = hasWindow;
        logger.log("View attachment check:", { hasWindow, isAttached });
      }
    } catch (e) {
      logger.error("Error checking view attachment:", e);
    }
  }
  
  // Force map remount by changing key when returning to home tab (fixes black screen)
  // This is the most reliable way to fix the black screen after tab switch
  if (mapbox.value) {
    logger.log("Forcing map remount by changing key");
    // Increment key to force Vue to completely remount the Mapbox component
    mapKey.value++;
    // Reset map references since component will be remounted
    mapbox.value = null;
    mapInstance.value = null;
    mapLoading.value = true;
  }
};
</script>