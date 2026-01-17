<template>
  <GridLayout rows="*, auto">
    <!-- Mapbox Map Container -->
    <GridLayout row="0">
      <Mapbox
        :key="mapKey"
        ref="mapbox"
        :accessToken="mapboxToken"
        :mapStyle="currentMapStyle"
        :latitude="41.9981"
        :longitude="21.4254"
        :zoomLevel="currentZoom"
        :showUserLocation="true"
        :hideCompass="false"
        :hideAttribution="false"
        :hideLogo="false"
        @mapReady="onMapReady"
        @markerSelect="onMarkerSelect"
        @mapError="onMapError"
      />
      
      <!-- Map Loading Skeleton Overlay -->
      <SkeletonLoader 
        v-show="mapLoading" 
        type="map" 
        containerStyle="position: absolute; width: 100%; height: 100%; background-color: rgba(245, 245, 245, 0.95);"
      />
      
      <!-- Map Controls Panel -->
      <StackLayout 
        horizontalAlignment="left"
        verticalAlignment="top"
        margin="15"
        style="background-color: rgba(255, 255, 255, 0.95); border-radius: 12; padding: 8;"
      >
        <!-- Zoom Controls -->
        <StackLayout>
          <Button 
            text="+"
            class="btn-primary"
            style="width: 45; height: 45; border-radius: 8; background-color: #000000; color: #ffffff; font-size: 24; font-weight: bold; padding: 0; margin-bottom: 4;"
            @tap="zoomIn"
          />
          <Button 
            text="−"
            class="btn-primary"
            style="width: 45; height: 45; border-radius: 8; background-color: #000000; color: #ffffff; font-size: 24; font-weight: bold; padding: 0;"
            @tap="zoomOut"
          />
        </StackLayout>
        
        <!-- Divider -->
        <StackLayout 
          style="height: 1; background-color: #e0e0e0; margin: 8 0;"
        />
        
         <!-- My Location Button -->
         <Button 
           text="📍"
           class="btn-primary"
           style="width: 45; height: 45; border-radius: 8; background-color: #000000; color: #ffffff; font-size: 20; padding: 0;"
           @tap="centerOnMyLocation"
         />
       </StackLayout>
     </GridLayout>

    <!-- Delivery Panel (Native UI) -->
    <GridLayout
      v-if="selectedDelivery"
      row="1"
      rows="auto, auto, auto"
      columns="*, *"
      backgroundColor="white"
      padding="20"
      class="delivery-panel"
    >
      <Label
        :text="selectedDelivery.title"
        row="0"
        colSpan="2"
        fontSize="18"
        fontWeight="bold"
        marginBottom="8"
        class="delivery-title"
      />
      <Label
        :text="selectedDelivery.description"
        row="1"
        colSpan="2"
        fontSize="14"
        color="#666"
        marginBottom="16"
        textWrap="true"
        class="delivery-description"
      />
      <Button
        text="Dismiss"
        row="2"
        col="0"
        marginRight="8"
        @tap="onDismiss"
        class="btn-reject"
      />
      <Button
        text="Accept Delivery"
        row="2"
        col="1"
        marginLeft="8"
        @tap="onAccept"
        class="btn-accept"
      />
    </GridLayout>
  </GridLayout>
</template>

<script setup lang="ts">
import { alert, Application } from "@nativescript/core";
import { ref, computed, onMounted, onUnmounted } from "vue";
import {
  enableLocationRequest,
  getCurrentLocation,
  watchLocation,
  clearWatch,
  isEnabled,
} from "@nativescript/geolocation";
import * as DeliveryService from "../services/deliveries.service";
import * as LocationService from "../services/location.service";
import { useDeliveriesStore } from "~/stores/useDeliveryStore";
import { useKYCStore } from "~/stores/kyc.store";
import { logger } from "../utils/logger";
import SkeletonLoader from "../components/SkeletonLoader.vue";

const store = useDeliveriesStore();
const kycStore = useKYCStore();

// Get Mapbox token from environment variable
const mapboxToken = (process.env.MAPBOX_ACCESS_TOKEN as string) || "";

const mapbox = ref<any>(null);
const mapInstance = ref<any>(null);
const deliveries = ref<any[]>([]);
const activeDelivery = ref<any | null>(null);
const selectedDelivery = ref<any | null>(null);
const deliveryMarkers = ref<Map<number, any>>(new Map());
const routeLine = ref<any>(null);
const pickupMarker = ref<any>(null);
const dropoffMarker = ref<any>(null);

const lastLoc = ref<{ lat: number; lng: number } | null>(null);
const userLocation = ref<{ lat: number; lng: number } | null>(null);
const assigning = ref(false);
const assignedIds = new Set<number>();
const mapLoading = ref(true);
const mapKey = ref(0); // Key to force remount when tab is activated

// Map controls
const currentZoom = ref(13);
const currentMapStyle = ref("streets");
const mapStyles = ["streets", "outdoors", "light", "dark", "satellite"];
const mapStyleIndex = ref(0);

const mapStyleIcon = computed(() => {
  const icons: { [key: string]: string } = {
    streets: "🗺️",
    outdoors: "🏔️",
    light: "☀️",
    dark: "🌙",
    satellite: "🛰️"
  };
  return icons[currentMapStyle.value] || "🗺️";
});

function onMapReady(args: any) {
  mapInstance.value = args.map;
  mapLoading.value = false;
  logger.log("Mapbox map ready");
  
  // Get initial zoom level if available
  if (mapInstance.value && typeof mapInstance.value.getZoomLevel === 'function') {
    try {
      const zoom = mapInstance.value.getZoomLevel();
      if (zoom) {
        currentZoom.value = zoom;
      }
    } catch (e) {
      // Ignore if not available
    }
  }
  
  // Store reference to mapbox component
  if (args.object) {
    mapbox.value = args.object;
    logger.log("Mapbox component stored:", {
      hasNativeView: !!args.object.nativeView,
      hasMap: !!mapInstance.value
    });
    
    // Note: Attachment listener removed - we handle refresh in __homeTabActivated instead
    // to avoid conflicts and ensure proper timing
  }
  
  // Set initial camera position if we have location and zoom in
  if (lastLoc.value && mapInstance.value) {
    setUserLocation(lastLoc.value.lat, lastLoc.value.lng);
    // Zoom in on current location
    setTimeout(() => {
      if (mapInstance.value) {
        try {
          // Set zoom level to 15 for a closer view
          currentZoom.value = 15;
          if (typeof mapInstance.value.setZoomLevel === 'function') {
            mapInstance.value.setZoomLevel({
              level: 15,
              animated: true
            });
          } else if (typeof mapInstance.value.zoomTo === 'function') {
            mapInstance.value.zoomTo(15, true);
          }
          // Center on location with zoom
          if (mapInstance.value.setCenter) {
            mapInstance.value.setCenter({
              lat: lastLoc.value!.lat,
              lng: lastLoc.value!.lng,
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

function onMarkerSelect(args: any) {
  const marker = args.marker;
  const deliveryId = marker.id;
  
  if (deliveryId) {
    const delivery = deliveries.value.find((d) => d.id === deliveryId);
    if (delivery) {
      selectedDelivery.value = delivery;
    }
  }
}

const hasCenteredOnInitialLocation = ref(false);

function setUserLocation(lat: number, lng: number, shouldCenter: boolean = false) {
  if (!mapInstance.value) return;
  
  lastLoc.value = { lat, lng };
  userLocation.value = { lat, lng };
  
  // Only center on initial location (first time), not on every update
  if (shouldCenter && !hasCenteredOnInitialLocation.value) {
    mapInstance.value.setCenter({
      lat,
      lng,
      animated: true,
    });
    hasCenteredOnInitialLocation.value = true;
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
      currentZoom.value = 15;
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

function zoomIn() {
  if (!mapInstance.value) return;
  
  const newZoom = Math.min(currentZoom.value + 1, 20);
  currentZoom.value = newZoom;
  
  try {
    if (typeof mapInstance.value.setZoomLevel === 'function') {
      mapInstance.value.setZoomLevel({
        level: newZoom,
        animated: true
      });
    } else if (typeof mapInstance.value.zoomTo === 'function') {
      mapInstance.value.zoomTo(newZoom, true);
    } else if (mapbox.value && typeof mapbox.value.setZoomLevel === 'function') {
      mapbox.value.setZoomLevel(newZoom);
    }
  } catch (e) {
    logger.error("Error zooming in:", e);
  }
}

function zoomOut() {
  if (!mapInstance.value) return;
  
  const newZoom = Math.max(currentZoom.value - 1, 3);
  currentZoom.value = newZoom;
  
  try {
    if (typeof mapInstance.value.setZoomLevel === 'function') {
      mapInstance.value.setZoomLevel({
        level: newZoom,
        animated: true
      });
    } else if (typeof mapInstance.value.zoomTo === 'function') {
      mapInstance.value.zoomTo(newZoom, true);
    } else if (mapbox.value && typeof mapbox.value.setZoomLevel === 'function') {
      mapbox.value.setZoomLevel(newZoom);
    }
  } catch (e) {
    logger.error("Error zooming out:", e);
  }
}

async function refreshNearby(lat: number, lng: number) {
  if (activeDelivery.value) return;
  
  try {
    const res = await DeliveryService.getNearbyDeliveries(lat, lng);
    
    deliveries.value = res.map((d: any) => ({
      id: d.id,
      title: d.title,
      description: d.pickupAddress,
      lat: d.pickupLatitude,
      lng: d.pickupLongitude,
    }));
    
    updateDeliveryMarkers();
  } catch (error) {
    logger.error("Failed to fetch nearby deliveries:", error);
  }
}

function updateDeliveryMarkers() {
  if (!mapInstance.value) return;
  
  const alive = new Set(deliveries.value.map((d) => d.id));
  
  // Remove markers that are no longer in the list
  for (const [id, marker] of deliveryMarkers.value.entries()) {
    if (!alive.has(id)) {
      try {
        mapInstance.value.removeMarkers([marker]);
        deliveryMarkers.value.delete(id);
      } catch (e) {
        logger.error("Error removing marker:", e);
      }
    }
  }
  
  // Add or update markers
  for (const delivery of deliveries.value) {
    if (!deliveryMarkers.value.has(delivery.id)) {
      // Add new marker
      try {
        const markers = mapInstance.value.addMarkers([
          {
            id: delivery.id.toString(),
            lat: delivery.lat,
            lng: delivery.lng,
            title: "📦",
            subtitle: delivery.title || delivery.description,
            onTap: () => {
              selectedDelivery.value = delivery;
            },
          },
        ]);
        if (markers && markers.length > 0) {
          deliveryMarkers.value.set(delivery.id, markers[0]);
        }
      } catch (e) {
        logger.error("Error adding marker:", e);
      }
    } else {
      // Update existing marker
      try {
        mapInstance.value.updateMarker(deliveryMarkers.value.get(delivery.id), {
          lat: delivery.lat,
          lng: delivery.lng,
        });
      } catch (e) {
        logger.error("Error updating marker:", e);
      }
    }
  }
}

async function drawRoute(fromLat: number, fromLng: number, toLat: number, toLng: number, shouldCenter: boolean = false) {
  if (!mapInstance.value) return;

  const url = `https://router.project-osrm.org/route/v1/driving/${fromLng},${fromLat};${toLng},${toLat}?overview=full&geometries=geojson`;
  const res = await fetch(url);
  const json = await res.json();

  if (!json.routes?.length) return;

  // IMPORTANT: Mapbox NativeScript expects {lat, lng} objects, not [lat, lng] arrays
  const points = json.routes[0].geometry.coordinates.map(
    ([lng, lat]: [number, number]) => ({ lat, lng })
  );

  clearRoute();

  routeLine.value = mapInstance.value.addPolyline({
    id: "route",
    points,
    color: "#FF0000",
    width: 6,
    opacity: 1
  });

  // Only center if explicitly requested (for new routes, not GPS updates)
  if (shouldCenter) {
    mapInstance.value.setCenter({
      lat: fromLat,
      lng: fromLng,
      animated: true
    });
  }
}

function clearRoute() {
  if (!mapInstance.value || !routeLine.value) return;

  mapInstance.value.removePolylines([routeLine.value]);
  routeLine.value = null;
}

function updateActiveDeliveryMarkers() {
  if (!mapInstance.value || !activeDelivery.value) {
    // Remove markers if no active delivery
    if (pickupMarker.value) {
      try {
        mapInstance.value.removeMarkers([pickupMarker.value]);
      } catch (e) {
        logger.error("Error removing pickup marker:", e);
      }
      pickupMarker.value = null;
    }
    if (dropoffMarker.value) {
      try {
        mapInstance.value.removeMarkers([dropoffMarker.value]);
      } catch (e) {
        logger.error("Error removing dropoff marker:", e);
      }
      dropoffMarker.value = null;
    }
    return;
  }

  const delivery = activeDelivery.value;

  // Update or create pickup marker
  if (pickupMarker.value) {
    try {
      mapInstance.value.updateMarker(pickupMarker.value, {
        lat: delivery.pickupLatitude,
        lng: delivery.pickupLongitude,
      });
    } catch (e) {
      logger.error("Error updating pickup marker:", e);
    }
  } else {
    try {
      const markers = mapInstance.value.addMarkers([
        {
          id: "active_pickup",
          lat: delivery.pickupLatitude,
          lng: delivery.pickupLongitude,
          title: "📍 Pickup",
          subtitle: delivery.pickupAddress || "Pickup Location",
          onTap: () => {
            logger.debug("Pickup location tapped");
          },
        },
      ]);
      if (markers && markers.length > 0) {
        pickupMarker.value = markers[0];
      }
    } catch (e) {
      logger.error("Error adding pickup marker:", e);
    }
  }

  // Update or create dropoff marker
  if (dropoffMarker.value) {
    try {
      mapInstance.value.updateMarker(dropoffMarker.value, {
        lat: delivery.dropoffLatitude,
        lng: delivery.dropoffLongitude,
      });
    } catch (e) {
      logger.error("Error updating dropoff marker:", e);
    }
  } else {
    try {
      const markers = mapInstance.value.addMarkers([
        {
          id: "active_dropoff",
          lat: delivery.dropoffLatitude,
          lng: delivery.dropoffLongitude,
          title: "🎯 Dropoff",
          subtitle: delivery.dropoffAddress || "Dropoff Location",
          onTap: () => {
            logger.debug("Dropoff location tapped");
          },
        },
      ]);
      if (markers && markers.length > 0) {
        dropoffMarker.value = markers[0];
      }
    } catch (e) {
      logger.error("Error adding dropoff marker:", e);
    }
  }
}

function clearDeliveries() {
  if (!mapInstance.value) return;
  
  try {
    const markers = Array.from(deliveryMarkers.value.values());
    if (markers.length > 0) {
      mapInstance.value.removeMarkers(markers);
    }
    deliveryMarkers.value.clear();
    deliveries.value = [];
  } catch (e) {
    logger.error("Error clearing deliveries:", e);
  }
}

const hasLoadedActiveDelivery = ref(false);

async function loadActiveDelivery(shouldCenter: boolean = false) {
  try {
    const active = await DeliveryService.getActiveDelivery();
    if (!active) {
      activeDelivery.value = null;
      clearRoute();
      updateActiveDeliveryMarkers();
      hasLoadedActiveDelivery.value = false;
      return false;
    }
    
    // Only center if this is the first time loading an active delivery
    const isNewDelivery = !hasLoadedActiveDelivery.value || activeDelivery.value?.id !== active.id;
    hasLoadedActiveDelivery.value = true;
    
    activeDelivery.value = active;
    clearDeliveries();
    
    // Update pickup and dropoff markers
    updateActiveDeliveryMarkers();
    
    // Fix 3: Use correct routing logic based on status
    let fromLat, fromLng, toLat, toLng;
    
    if (active.status === "ASSIGNED") {
      // Route from driver's current location to pickup
      fromLat = lastLoc.value?.lat ?? active.pickupLatitude;
      fromLng = lastLoc.value?.lng ?? active.pickupLongitude;
      toLat = active.pickupLatitude;
      toLng = active.pickupLongitude;
    } else {
      // Route from driver's current location to dropoff
      fromLat = lastLoc.value?.lat ?? active.dropoffLatitude;
      fromLng = lastLoc.value?.lng ?? active.dropoffLongitude;
      toLat = active.dropoffLatitude;
      toLng = active.dropoffLongitude;
    }
    
    // Only center if explicitly requested AND it's a new delivery
    await drawRoute(fromLat, fromLng, toLat, toLng, shouldCenter && isNewDelivery);
    
    return true;
  } catch (error) {
    logger.error("Failed to load active delivery:", error);
    return false;
  }
}

// Handle app lifecycle events
let resumeHandler: any = null;
let locationWatcher: number | null = null;

onMounted(() => {
  // Set up app resume handler - ONLY reload style on resume
  resumeHandler = Application.on(Application.resumeEvent, () => {
    if (!mapbox.value) return;
    
    // Single style reload after resume (300ms delay to let app settle)
    setTimeout(() => {
      if (mapbox.value) {
        // Force Vue to update the mapStyle prop by temporarily changing it
        const tempStyle = currentMapStyle.value;
        currentMapStyle.value = mapStyles[(mapStyleIndex.value + 1) % mapStyles.length];
        setTimeout(() => {
          currentMapStyle.value = tempStyle;
        }, 50);
      }
    }, 300);
  });
});

onUnmounted(() => {
  // Clean up location watcher
  if (locationWatcher !== null) {
    try {
      clearWatch(locationWatcher);
      locationWatcher = null;
    } catch (e) {
      logger.error("Error clearing location watcher:", e);
    }
  }
  
  // Clean up event handlers
  if (resumeHandler) {
    Application.off(Application.resumeEvent, resumeHandler);
    resumeHandler = null;
  }
});

(globalThis as any).__homeTabActivated = async () => {
  logger.log("=== HOME TAB ACTIVATED ===");
  
  // Reload KYC status when home tab is activated
  kycStore.load();
  
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
  
  // Refresh data (don't center on tab activation)
  const hasActive = await loadActiveDelivery(false);
  if (hasActive) return;
  if (!lastLoc.value) return;
  
  await refreshNearby(lastLoc.value.lat, lastLoc.value.lng);
};

(globalThis as any).__deliveryStatusChanged = async (d: any) => {
  if (d.status === "CANCELLED" || d.status === "DELIVERED") {
    activeDelivery.value = null;
    clearRoute();
    updateActiveDeliveryMarkers();
    if (lastLoc.value) {
      await refreshNearby(lastLoc.value.lat, lastLoc.value.lng);
    }
    return;
  }
  
  activeDelivery.value = d;
  
  // Update pickup and dropoff markers
  updateActiveDeliveryMarkers();
  
  // Fix: Correct pickup vs dropoff routing logic
  let fromLat, fromLng, toLat, toLng;
  
  if (d.status === "ASSIGNED") {
    // Route from driver's current location to pickup
    fromLat = lastLoc.value?.lat ?? d.pickupLatitude;
    fromLng = lastLoc.value?.lng ?? d.pickupLongitude;
    toLat = d.pickupLatitude;
    toLng = d.pickupLongitude;
  } else {
    // Route from driver's current location to dropoff
    fromLat = lastLoc.value?.lat ?? d.dropoffLatitude;
    fromLng = lastLoc.value?.lng ?? d.dropoffLongitude;
    toLat = d.dropoffLatitude;
    toLng = d.dropoffLongitude;
  }
  
  clearRoute();
  await drawRoute(fromLat, fromLng, toLat, toLng, true);
};

function onDismiss() {
  selectedDelivery.value = null;
}

async function onAccept() {
  if (!selectedDelivery.value) return;
  if (assigning.value || assignedIds.has(selectedDelivery.value.id)) return;
  
  // Check KYC status before accepting delivery
  await kycStore.load();
  
  if (kycStore.status !== 'APPROVED') {
    let message = 'KYC verification is required to accept deliveries.';
    if (kycStore.status === 'REJECTED') {
      message = `KYC verification was rejected. ${kycStore.rejectionReason || 'Please resubmit your KYC documents.'}`;
    } else if (kycStore.status === 'PENDING') {
      message = 'Your KYC verification is pending. Please wait for approval.';
    } else if (kycStore.status === null) {
      message = 'Please complete KYC verification to accept deliveries.';
    }
    alert(message);
    return;
  }
  
  assigning.value = true;
  assignedIds.add(selectedDelivery.value.id);
  
  try {
    const assigned = await DeliveryService.assignDelivery(selectedDelivery.value.id);
    
    activeDelivery.value = assigned;
    selectedDelivery.value = null;
    
    alert("Delivery assigned. Navigate to pickup.");
    
    clearDeliveries();
    
    if (lastLoc.value) {
      await drawRoute(
        lastLoc.value.lat,
        lastLoc.value.lng,
        assigned.dropoffLatitude,
        assigned.dropoffLongitude
      );
    } else {
      await drawRoute(
        assigned.pickupLatitude,
        assigned.pickupLongitude,
        assigned.dropoffLatitude,
        assigned.dropoffLongitude
      );
    }
    
    store.loadAssigned(true);
  } catch (err) {
    await alert(String((err as any)?.message || "Assign failed"));
    assignedIds.delete(selectedDelivery.value.id);
  } finally {
    assigning.value = false;
  }
}

async function pushLocation(lat: number, lng: number) {
  try {
    await LocationService.updateDriverLocation(lat, lng);
  } catch (e) {
    logger.error("Failed to push location:", e);
  }
}

onMounted(async () => {
  const enabled = await isEnabled();
  if (!enabled) {
    try {
      await enableLocationRequest(false, false);
      const stillDisabled = !(await isEnabled());
      if (stillDisabled) {
        logger.warn("Location permission denied on mount");
      }
    } catch (e) {
      logger.error("Error requesting location permission:", e);
    }
  }
  
  await loadActiveDelivery();
  
  if (await isEnabled()) {
    try {
      const loc = await getCurrentLocation({ timeout: 20000 });
      lastLoc.value = { lat: loc.latitude, lng: loc.longitude };
      
      setUserLocation(loc.latitude, loc.longitude, true);
      pushLocation(loc.latitude, loc.longitude);
      
      if (!activeDelivery.value) {
        await refreshNearby(loc.latitude, loc.longitude);
      }
    } catch (e) {
      logger.error("Failed to get current location:", e);
    }
  }
  
  let lastNearbyFetch = 0;
  
  if (await isEnabled()) {
    try {
      const watcherId = await watchLocation(
        async (l) => {
          lastLoc.value = { lat: l.latitude, lng: l.longitude };
          userLocation.value = { lat: l.latitude, lng: l.longitude };
          
          setUserLocation(l.latitude, l.longitude, false);
          pushLocation(l.latitude, l.longitude);
          
          if (activeDelivery.value) {
            // Only draw route if it doesn't exist (don't redraw every GPS tick)
            if (!routeLine.value) {
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
              
              await drawRoute(l.latitude, l.longitude, target.lat, target.lng, false);
            }
          } else {
            const now = Date.now();
            if (now - lastNearbyFetch > 8000) {
              lastNearbyFetch = now;
              await refreshNearby(l.latitude, l.longitude);
            }
          }
        },
        () => {},
        { minimumUpdateTime: 2000, desiredAccuracy: 3 }
      );
      locationWatcher = watcherId;
    } catch (e) {
      logger.error("Failed to start location watcher:", e);
    }
  }
});
</script>

<style scoped>
.delivery-panel {
  border-top-width: 2;
  border-top-color: #000;
  border-radius: 16 16 0 0;
}

.delivery-title {
  color: #000;
}

.delivery-description {
  color: #666;
}

.btn-reject {
  background-color: #ffffff;
  color: #000000;
  border-width: 2;
  border-color: #000000;
  border-radius: 12;
  padding: 14 20;
  font-size: 15;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5;
}

.btn-accept {
  background-color: #000000;
  color: #ffffff;
  border-radius: 12;
  padding: 14 20;
  font-size: 15;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5;
}
</style>