<template>
  <GridLayout rows="*, auto">
    <!-- Mapbox Map Container -->
    <GridLayout row="0">
      <!-- Map Loading Skeleton -->
      <SkeletonLoader 
        v-if="mapLoading" 
        type="map" 
        containerStyle="width: 100%; height: 100%;"
      />
      
      <Mapbox
        v-show="!mapLoading"
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
        
        <!-- Map Style Switcher -->
        <Button 
          :text="mapStyleIcon"
          class="btn-primary"
          style="width: 45; height: 45; border-radius: 8; background-color: #000000; color: #ffffff; font-size: 20; padding: 0; margin-bottom: 4;"
          @tap="toggleMapStyle"
        />
        
         <!-- My Location Button -->
         <Button 
           text="📍"
           class="btn-primary"
           style="width: 45; height: 45; border-radius: 8; background-color: #000000; color: #ffffff; font-size: 20; padding: 0; margin-bottom: 4;"
           @tap="centerOnMyLocation"
         />
         
         <!-- Refresh Map Button (for black screen recovery) -->
         <Button 
           :text="isRefreshing ? '⏳' : '🔄'"
           :isEnabled="!isRefreshing"
           class="btn-primary"
           style="width: 45; height: 45; border-radius: 8; background-color: #000000; color: #ffffff; font-size: 20; padding: 0;"
           @tap="forceMapRefresh"
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
import { alert, Application, Page } from "@nativescript/core";
import { ref, computed, onMounted, onUnmounted, getCurrentInstance } from "vue";
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
import { ErrorHandler } from "../utils/errorHandler";
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
  mapLoading.value = false; // Hide skeleton when map is ready
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
    // Ensure map view is visible and properly initialized
    if (args.object.nativeView) {
      try {
        const nativeView = args.object.nativeView;
        // Set visibility
        if (nativeView.setVisibility) {
          nativeView.setVisibility(android.view.View.VISIBLE);
        }
        // Ensure view is properly attached
        if (nativeView.getParent && typeof nativeView.getParent === 'function') {
          try {
            const parent = nativeView.getParent();
            if (!parent && nativeView.getWindow) {
              // View might not be attached yet, schedule a retry
              setTimeout(() => {
                refreshMapVisibility();
              }, 100);
            }
          } catch (e) {
            // Ignore getParent errors
          }
        }
        // Set up view attachment listener (Android)
        if (nativeView.setOnAttachStateChangeListener) {
          nativeView.setOnAttachStateChangeListener(
            new android.view.View.OnAttachStateChangeListener({
              onViewAttachedToWindow: () => {
                // View attached - ensure map is visible
                setTimeout(() => refreshMapVisibility(), 50);
              },
              onViewDetachedFromWindow: () => {
                // View detached - will be refreshed on reattach
              }
            })
          );
        }
      } catch (e) {
        logger.error("Error in onMapReady setup:", e);
      }
    }
  }
  
  // Set initial camera position if we have location
  if (lastLoc.value) {
    setUserLocation(lastLoc.value.lat, lastLoc.value.lng);
  }
}

function onMapError(args: any) {
  // Handle map errors (style loading failures, etc.)
  logger.error("Map error occurred:", args);
  // Try to refresh the map after a short delay
  setTimeout(() => {
    refreshMapVisibility();
  }, 500);
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
  // The blue dot from :showUserLocation="true" already shows the user's location
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
        // Request permission without opening settings
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
  
  if (mapInstance.value?.setCenter && userLocation.value) {
    mapInstance.value.setCenter({
      lat: userLocation.value.lat,
      lng: userLocation.value.lng,
      animated: true
    });
  }
}

function zoomIn() {
  if (!mapInstance.value) return;
  
  const newZoom = Math.min(currentZoom.value + 1, 20);
  currentZoom.value = newZoom;
  
  try {
    // Try multiple zoom methods
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
    // Try multiple zoom methods
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

function toggleMapStyle() {
  mapStyleIndex.value = (mapStyleIndex.value + 1) % mapStyles.length;
  currentMapStyle.value = mapStyles[mapStyleIndex.value];
  
  // Update map style if map is ready
  // Try multiple methods as different plugins may have different APIs
  if (mapInstance.value) {
    try {
      // Method 1: Try setMapStyle
      if (typeof mapInstance.value.setMapStyle === 'function') {
        mapInstance.value.setMapStyle(currentMapStyle.value);
      }
      // Method 2: Try updateMapStyle
      else if (typeof mapInstance.value.updateMapStyle === 'function') {
        mapInstance.value.updateMapStyle(currentMapStyle.value);
      }
      // Method 3: Try setting style on mapbox component
      else if (mapbox.value && typeof mapbox.value.setMapStyle === 'function') {
        mapbox.value.setMapStyle(currentMapStyle.value);
      }
      // Method 4: Try native view
      else if (mapbox.value?.nativeView) {
        const nativeView = mapbox.value.nativeView;
        if (nativeView.setStyleUri) {
          // Mapbox Android SDK style
          const styleUri = `mapbox://styles/mapbox/${currentMapStyle.value}-v11`;
          nativeView.setStyleUri(styleUri);
        }
      }
      
      // Force refresh after style change
      setTimeout(() => {
        refreshMapVisibility();
      }, 200);
    } catch (e) {
      logger.error("Error changing map style:", e);
    }
  }
}

const isRefreshing = ref(false);

function forceMapRefresh() {
  // Prevent multiple simultaneous refresh attempts
  if (isRefreshing.value) {
    logger.log("Refresh already in progress, skipping...");
    return;
  }
  
  isRefreshing.value = true;
  logger.log("Force refreshing map with aggressive recovery...");
  
  if (!mapbox.value || !mapbox.value.nativeView) {
    logger.error("Map view not available for refresh");
    isRefreshing.value = false;
    return;
  }
  
  try {
    const nativeView = mapbox.value.nativeView;
    
    // Strategy 1: Try plugin-level style reload first (more reliable)
    if (mapbox.value && typeof mapbox.value.setMapStyle === 'function') {
      logger.log("Attempting plugin-level style reload...");
      try {
        // Cycle through styles to force reload
        const styles = ["outdoors", "streets", "light", "dark", "satellite"];
        const currentStyleIndex = styles.indexOf(currentMapStyle.value);
        const nextStyle = styles[(currentStyleIndex + 1) % styles.length];
        
        // Change to different style
        mapbox.value.setMapStyle(nextStyle);
        setTimeout(() => {
          // Change back to original
          if (mapbox.value && typeof mapbox.value.setMapStyle === 'function') {
            mapbox.value.setMapStyle(currentMapStyle.value);
            logger.log(`Style cycled: ${nextStyle} -> ${currentMapStyle.value}`);
          }
        }, 200);
      } catch (pluginError) {
        logger.error("Plugin style reload failed:", pluginError);
      }
    }
    
    // Strategy 2: Hide and show the view to force a complete redraw
    if (nativeView.setVisibility) {
      logger.log("Hiding and showing map view...");
      nativeView.setVisibility(android.view.View.GONE);
      setTimeout(() => {
        if (nativeView.setVisibility) {
          nativeView.setVisibility(android.view.View.VISIBLE);
        }
        refreshMapVisibility();
      }, 150);
    }
    
    // Strategy 3: Force complete style reload via native SDK (with better error handling)
    if (mapInstance.value) {
      try {
        const nativeMap = mapInstance.value.nativeMapView || 
                         (nativeView.getMap && nativeView.getMap());
        if (nativeMap) {
          logger.log("Attempting native SDK style reload...");
          
          // Get current camera position
          let currentLat = 41.9981;
          let currentLng = 21.4254;
          let currentZoomLevel = currentZoom.value || 13;
          
          if (nativeMap.getCameraPosition) {
            try {
              const camPos = nativeMap.getCameraPosition();
              if (camPos && camPos.target) {
                currentLat = camPos.target.getLatitude();
                currentLng = camPos.target.getLongitude();
                currentZoomLevel = camPos.zoom || currentZoomLevel;
                logger.log(`Current camera: ${currentLat}, ${currentLng}, zoom: ${currentZoomLevel}`);
              }
            } catch (e) {
              logger.warn("Could not get camera position, using defaults");
            }
          }
          
          // Try multiple native SDK approaches
          try {
            // Method 1: Try Style.Builder
            const Style = (android as any).mapbox?.mapboxsdk?.maps?.Style;
            if (Style && nativeMap.setStyle) {
              const styleUri = `mapbox://styles/mapbox/${currentMapStyle.value}-v11`;
              logger.log(`Reloading map style via native SDK: ${styleUri}`);
              
              const styleBuilder = new Style.Builder().fromUri(styleUri);
              nativeMap.setStyle(styleBuilder.build());
              
              // Wait for style to load, then reset camera
              setTimeout(() => {
                try {
                  const CameraUpdateFactory = (android as any).mapbox?.mapboxsdk?.camera?.CameraUpdateFactory;
                  if (CameraUpdateFactory && nativeMap.moveCamera) {
                    const LatLng = (android as any).mapbox?.mapboxsdk?.geometry?.LatLng;
                    if (LatLng) {
                      const latLng = new LatLng(currentLat, currentLng);
                      const cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, currentZoomLevel);
                      nativeMap.moveCamera(cameraUpdate);
                      logger.log("Camera reset after style reload");
                    }
                  }
                } catch (camError) {
                  logger.error("Error resetting camera:", camError);
                }
              }, 500);
            } else {
              logger.warn("Native SDK Style class or setStyle method not available");
            }
          } catch (styleError: any) {
            logger.error("Native SDK style reload failed:", styleError?.message || String(styleError));
            
            // Fallback: Try triggerRepaint
            try {
              if (nativeMap.triggerRepaint) {
                logger.log("Attempting triggerRepaint as fallback...");
                nativeMap.triggerRepaint();
              }
            } catch (repaintError) {
              logger.error("triggerRepaint also failed:", repaintError);
            }
          }
        } else {
          logger.warn("Native map instance not available");
        }
      } catch (nativeError: any) {
        logger.error("Error accessing native map:", nativeError?.message || String(nativeError));
      }
    } else {
      logger.warn("Map instance not available");
    }
    
    // Strategy 4: Multiple refresh attempts with increasing delays
    setTimeout(() => {
      refreshMapVisibility();
    }, 300);
    setTimeout(() => {
      refreshMapVisibility();
    }, 600);
    setTimeout(() => {
      refreshMapVisibility();
      
      // Check if map is still black after refresh attempts
      setTimeout(() => {
        let mapRecovered = false;
        try {
          const nativeMap = mapInstance.value?.nativeMapView || 
                           (nativeView.getMap && nativeView.getMap());
          if (nativeMap) {
            try {
              const style = nativeMap.getStyle();
              if (style) {
                mapRecovered = true;
                logger.log("Map style is accessible - map should be visible");
              } else {
                logger.warn("Map style is null after refresh");
              }
            } catch (styleError: any) {
              logger.warn(`Cannot access map style: ${styleError?.message || String(styleError)}`);
            }
          } else {
            logger.warn("Native map instance not available for verification");
          }
        } catch (checkError: any) {
          logger.error("Error checking map recovery:", checkError?.message || String(checkError));
        }
        
        if (!mapRecovered) {
          logger.warn("Map recovery failed - suggesting user actions");
          alert({
            title: "Map Recovery",
            message: "The map couldn't be automatically recovered. Please try:\n1. Switching to another tab and back\n2. Closing and reopening the app\n3. Restarting your device if the problem persists",
            okButtonText: "OK"
          });
        }
        
        isRefreshing.value = false;
      }, 800);
    }, 1200);
    
  } catch (e: any) {
    logger.error("Error in force refresh:", e?.message || String(e));
    isRefreshing.value = false;
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
    points,                 // ✅ CORRECT FORMAT: array of {lat, lng} objects
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
          // Note: To use emoji as icon, we need emoji image assets
          // For now, emoji appears in title/popup
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
          // Note: To use emoji as icon, we need emoji image assets
          // For now, emoji appears in title/popup
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
      updateActiveDeliveryMarkers(); // Remove pickup/dropoff markers
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
let pauseHandler: any = null;
let lowMemoryHandler: any = null;
let orientationHandler: any = null;
let pageLoadedHandler: any = null;
let visibilityCheckInterval: any = null;
let locationWatcher: number | null = null;
let pendingTimeouts: Set<ReturnType<typeof setTimeout>> = new Set();

onMounted(() => {
  // Set up periodic visibility check (every 2 seconds) as a safety net
  visibilityCheckInterval = setInterval(() => {
    if (mapbox.value && mapbox.value.nativeView) {
      try {
        const nativeView = mapbox.value.nativeView;
        // Check if view is visible but might be black
        if (nativeView.getVisibility && nativeView.getVisibility() === android.view.View.VISIBLE) {
          // View is marked visible, but check if it's actually rendering
          // If parent is visible and view has size, assume it's okay
          if (nativeView.getParent && typeof nativeView.getParent === 'function') {
            try {
              const parent = nativeView.getParent();
              if (parent && parent.getVisibility && typeof parent.getVisibility === 'function' && parent.getVisibility() !== android.view.View.VISIBLE) {
                // Parent is hidden - refresh when it becomes visible
                refreshMapVisibility();
              }
            } catch (e) {
              // Ignore getParent errors
            }
          }
        }
      } catch (e) {
        // Ignore errors in periodic check
      }
    }
  }, 2000);
  
  // Set up app resume handler to refresh map when app comes to foreground
  resumeHandler = Application.on(Application.resumeEvent, () => {
    // Aggressive refresh when app resumes (black screen often happens after resume)
    const timeout1 = setTimeout(() => {
      refreshMapVisibility();
      pendingTimeouts.delete(timeout1);
    }, 50);
    pendingTimeouts.add(timeout1);
    
    const timeout2 = setTimeout(() => {
      refreshMapVisibility();
      pendingTimeouts.delete(timeout2);
    }, 200);
    pendingTimeouts.add(timeout2);
    
    const timeout3 = setTimeout(() => {
      refreshMapVisibility();
      pendingTimeouts.delete(timeout3);
    }, 500);
    pendingTimeouts.add(timeout3);
  });
  
  // Set up app pause handler
  pauseHandler = Application.on(Application.suspendEvent, () => {
    // Map will be refreshed on resume
  });
  
  // Handle low memory warnings (Android can destroy views under memory pressure)
  lowMemoryHandler = Application.on(Application.lowMemoryEvent, () => {
    logger.warn("Low memory warning - refreshing map");
    // Refresh map after low memory event
    setTimeout(() => {
      refreshMapVisibility();
    }, 200);
  });
  
  // Handle orientation changes (can cause map to go black)
  if (Application.android) {
    try {
      const activity = Application.android.foregroundActivity || Application.android.startActivity;
      if (activity) {
        const configuration = activity.getResources().getConfiguration();
        let lastOrientation = configuration.orientation;
        
        // Monitor orientation changes via a periodic check (Android doesn't have direct orientation event)
        const orientationCheckInterval = setInterval(() => {
          if (activity && !activity.isDestroyed()) {
            const currentConfig = activity.getResources().getConfiguration();
            if (currentConfig.orientation !== lastOrientation) {
              lastOrientation = currentConfig.orientation;
              logger.log("Orientation changed - refreshing map");
              setTimeout(() => {
                refreshMapVisibility();
              }, 300);
            }
          } else {
            clearInterval(orientationCheckInterval);
          }
        }, 500);
        
        // Store interval for cleanup
        orientationHandler = orientationCheckInterval;
      }
    } catch (e) {
      logger.error("Error setting up orientation handler:", e);
    }
  }
  
  // Handle page loaded event (when page becomes visible)
  const instance = getCurrentInstance();
  if (instance?.proxy) {
    const page = (instance.proxy as any).$page;
    if (page) {
      pageLoadedHandler = page.on(Page.loadedEvent, () => {
        // Page loaded - ensure map is visible
        setTimeout(() => {
          refreshMapVisibility();
        }, 100);
      });
    }
  }
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
  
  // Clean up all pending timeouts
  pendingTimeouts.forEach(timeout => {
    clearTimeout(timeout);
  });
  pendingTimeouts.clear();
  
  // Clean up event handlers
  if (resumeHandler) {
    Application.off(Application.resumeEvent, resumeHandler);
    resumeHandler = null;
  }
  if (pauseHandler) {
    Application.off(Application.suspendEvent, pauseHandler);
    pauseHandler = null;
  }
  if (lowMemoryHandler) {
    Application.off(Application.lowMemoryEvent, lowMemoryHandler);
    lowMemoryHandler = null;
  }
  if (orientationHandler) {
    clearInterval(orientationHandler);
    orientationHandler = null;
  }
  if (visibilityCheckInterval) {
    clearInterval(visibilityCheckInterval);
    visibilityCheckInterval = null;
  }
  if (pageLoadedHandler) {
    const instance = getCurrentInstance();
    if (instance?.proxy) {
      const page = (instance.proxy as any).$page;
      if (page) {
        page.off(Page.loadedEvent, pageLoadedHandler);
      }
    }
    pageLoadedHandler = null;
  }
});

function refreshMapVisibility() {
  // Comprehensive map refresh to fix black screen issues
  if (!mapbox.value || !mapbox.value.nativeView) {
    return;
  }
  
  try {
    const nativeView = mapbox.value.nativeView;
    
    // 1. Ensure view is visible
    if (nativeView.setVisibility) {
      nativeView.setVisibility(android.view.View.VISIBLE);
    }
    
    // 2. Ensure view has proper dimensions (not 0x0)
    if (nativeView.getWidth && nativeView.getHeight) {
      const width = nativeView.getWidth();
      const height = nativeView.getHeight();
      if (width === 0 || height === 0) {
        // View has no size - request layout
        if (nativeView.requestLayout) {
          nativeView.requestLayout();
        }
        // Retry after layout
        setTimeout(() => refreshMapVisibility(), 100);
        return;
      }
    }
    
    // 3. Ensure view is attached to window
    if (nativeView.getWindow && !nativeView.getWindow()) {
      // View not attached - will be handled by attachment listener
      return;
    }
    
    // 4. Force view to be brought to front (in case it's behind other views)
    if (nativeView.bringToFront) {
      nativeView.bringToFront();
    }
    
    // 5. Invalidate and request layout
    if (nativeView.invalidate) {
      nativeView.invalidate();
    }
    if (nativeView.requestLayout) {
      nativeView.requestLayout();
    }
    
    // 6. Try to refresh the map instance and force style reload
    if (mapInstance.value) {
      // Try various refresh methods
      if (typeof mapInstance.value.refresh === 'function') {
        mapInstance.value.refresh();
      }
      if (typeof mapInstance.value.invalidate === 'function') {
        mapInstance.value.invalidate();
      }
      
      // CRITICAL: Force map style to reload (black screen often means style is unloaded)
      try {
        // Try to reload the map style
        if (mapbox.value && typeof mapbox.value.setMapStyle === 'function') {
          // Temporarily change style to force reload
          const tempStyle = currentMapStyle.value === "streets" ? "outdoors" : "streets";
          mapbox.value.setMapStyle(tempStyle);
          setTimeout(() => {
            if (mapbox.value && typeof mapbox.value.setMapStyle === 'function') {
              mapbox.value.setMapStyle(currentMapStyle.value);
            }
          }, 100);
        }
      } catch (styleError) {
        // Ignore style reload errors
      }
      
      // Try to get native map and refresh it
      try {
        const nativeMap = mapInstance.value.nativeMapView || 
                         (nativeView.getMap && nativeView.getMap());
        if (nativeMap) {
          // Force style reload via native SDK
          try {
            const style = nativeMap.getStyle();
            if (style) {
              // Style exists, but might not be loaded - trigger a reload
              // Try to get a source to force style validation
              if (style.getSource && style.getLayer) {
                // Style is accessible, try to trigger a refresh
                nativeMap.triggerRepaint();
              }
            }
          } catch (styleCheckError) {
            // Style might not be loaded - try to reload it
            try {
              const Style = (android as any).mapbox?.mapboxsdk?.maps?.Style;
              if (Style && nativeMap.setStyle) {
                const styleUri = `mapbox://styles/mapbox/${currentMapStyle.value}-v11`;
                nativeMap.setStyle(new Style.Builder().fromUri(styleUri).build());
              }
            } catch (reloadError) {
              // Fallback: just trigger repaint
              if (nativeMap.triggerRepaint) {
                nativeMap.triggerRepaint();
              }
            }
          }
          
          // Trigger a camera update to force redraw
          if (nativeMap.getCameraPosition) {
            const currentPos = nativeMap.getCameraPosition();
            if (currentPos) {
              // Small camera update to force redraw
              const cameraUpdate = (android as any).mapbox?.mapboxsdk?.camera?.CameraUpdateFactory?.newLatLngZoom(
                currentPos.target,
                currentPos.zoom
              );
              if (cameraUpdate && nativeMap.moveCamera) {
                nativeMap.moveCamera(cameraUpdate);
              }
            }
          }
        }
      } catch (nativeError) {
        // Native map refresh failed - that's okay, we tried
      }
    }
    
    // 7. Force parent views to be visible too
    if (nativeView.getParent && typeof nativeView.getParent === 'function') {
      try {
        let parent = nativeView.getParent();
        while (parent) {
          if (parent.setVisibility && typeof parent.setVisibility === 'function') {
            parent.setVisibility(android.view.View.VISIBLE);
          }
          if (parent.getParent && typeof parent.getParent === 'function') {
            parent = parent.getParent();
          } else {
            break;
          }
        }
      } catch (e) {
        // Ignore parent traversal errors
      }
    }
    
  } catch (e) {
    logger.error("Error refreshing map visibility:", e);
  }
}

(globalThis as any).__homeTabActivated = async () => {
  // Reload KYC status when home tab is activated (in case it changed)
  kycStore.load();
  
  // Aggressive refresh when tab is activated (black screen fix)
  // Multiple refresh attempts with delays to ensure map recovers
  refreshMapVisibility();
  setTimeout(() => refreshMapVisibility(), 100);
  setTimeout(() => refreshMapVisibility(), 300);
  
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
    updateActiveDeliveryMarkers(); // Remove pickup/dropoff markers
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
  await drawRoute(fromLat, fromLng, toLat, toLng, true); // Center on status change
};

function onDismiss() {
  selectedDelivery.value = null;
}

async function onAccept() {
  if (!selectedDelivery.value) return;
  if (assigning.value || assignedIds.has(selectedDelivery.value.id)) return;
  
  // Check KYC status before accepting delivery
  // Reload KYC status in case it changed
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
      // Request permission without opening settings (second param = false)
      await enableLocationRequest(false, false);
      // Check again after request
      const stillDisabled = !(await isEnabled());
      if (stillDisabled) {
        logger.warn("Location permission denied on mount");
        // Don't show alert immediately - let user use app without location
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
      
      setUserLocation(loc.latitude, loc.longitude, true); // Center only on initial location
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
          
          setUserLocation(l.latitude, l.longitude, false); // Don't center on GPS updates
          pushLocation(l.latitude, l.longitude);
          
          if (activeDelivery.value) {
            // Fix 2: Only draw route if it doesn't exist (don't redraw every GPS tick)
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
              
              await drawRoute(l.latitude, l.longitude, target.lat, target.lng, false); // Don't center on GPS updates
            }
            // Note: For live navigation, you would update only the first point of the route,
            // not rebuild the entire polyline every 2 seconds
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
