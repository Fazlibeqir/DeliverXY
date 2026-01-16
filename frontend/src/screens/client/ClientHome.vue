<template>
  <GridLayout rows="auto, *, auto">
    <!-- Minimal Header -->
    <StackLayout row="0" class="p-3 bg-white" style="border-bottom-width: 1; border-bottom-color: #e0e0e0;">
      <Label text="📍 Select Locations" class="text-base font-bold" />
      <Label :text="instructionText" class="text-xs text-secondary mt-1" />
    </StackLayout>

    <!-- Map - Full Screen -->
    <GridLayout row="1">
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
        mapStyle="streets"
        :latitude="mapCenterLat"
        :longitude="mapCenterLng"
        :zoomLevel="13"
        :showUserLocation="true"
        @mapReady="onMapReady"
        @mapTap="onMapTap"
        @mapError="onMapError"
      />
      
      <!-- Floating Buttons -->
      <StackLayout 
        horizontalAlignment="right"
        verticalAlignment="bottom"
        margin="15"
      >
        <!-- Refresh Map Button (for black screen recovery) -->
        <Button 
          :text="isRefreshing ? '⏳' : '🔄'"
          :isEnabled="!isRefreshing"
          class="btn-primary"
          style="width: 50; height: 50; border-radius: 25; background-color: #000000; color: #ffffff; font-size: 20; padding: 0; margin-bottom: 10;"
          @tap="forceMapRefresh"
        />
        
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
import { alert, Application, Page } from "@nativescript/core";
import { $showModal } from "nativescript-vue";
import AddressSearchModal from "./AddressSearchModal.vue";
import DeliveryDetailsPage from "./DeliveryDetailsPage.vue";
import { logger } from "../../utils/logger";
import { ErrorHandler } from "../../utils/errorHandler";
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
  mapLoading.value = false; // Hide skeleton when map is ready
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
  
  // Try multiple methods to ensure tap events work
  if (mapInstance.value) {
    // Method 1: Try plugin's setOnMapClickListener (if available)
    if (typeof mapInstance.value.setOnMapClickListener === 'function') {
      try {
        mapInstance.value.setOnMapClickListener((point: any) => {
          if (point) {
            const lat = point.lat || point.latitude || (typeof point.getLatitude === 'function' ? point.getLatitude() : null);
            const lng = point.lng || point.longitude || (typeof point.getLongitude === 'function' ? point.getLongitude() : null);
            if (lat != null && lng != null) {
              onMapTap({ location: { latitude: lat, longitude: lng } });
            }
          }
        });
      } catch (e) {
        logger.error("Error setting up plugin listener:", e);
      }
    }
    
    // Method 2: Try native Android listener (delayed to ensure map is fully ready)
    setTimeout(() => {
      if (mapbox.value?.nativeView && mapInstance.value) {
        try {
          const nativeView = mapbox.value.nativeView;
          // Try to get native map from different possible locations
          const nativeMap = mapInstance.value.nativeMapView || 
                          (nativeView.getMap && nativeView.getMap()) ||
                          (mapInstance.value.map && mapInstance.value.map.nativeMapView);
          
          if (nativeMap && typeof nativeMap.addOnMapClickListener === 'function') {
            const MapboxMap = (android as any).mapbox?.mapboxsdk?.maps?.MapboxMap;
            if (MapboxMap) {
              const listener = new MapboxMap.OnMapClickListener({
                onMapClick: (point: any) => {
                  if (point && typeof point.getLatitude === 'function') {
                    const lat = point.getLatitude();
                    const lng = point.getLongitude();
                    onMapTap({ location: { latitude: lat, longitude: lng } });
                  }
                  return true;
                }
              });
              nativeMap.addOnMapClickListener(listener);
            }
          }
        } catch (nativeError) {
          // Native listener setup failed, @mapTap event will work as fallback
        }
      }
    }, 500); // Small delay to ensure map is fully initialized
  }
  
  loadCurrentLocation();
}

function onMapError(args: any) {
  // Handle map errors (style loading failures, etc.)
  logger.error("Map error occurred:", args);
  // Try to refresh the map after a short delay
  setTimeout(() => {
    refreshMapVisibility();
  }, 500);
}

async function loadCurrentLocation() {
  // Get location but don't auto-center - let user pan freely
  try {
    const enabled = await isEnabled();
    if (!enabled) {
      // Request permission without opening settings (second param = false)
      await enableLocationRequest(false, false);
      // Check again after request
      const stillDisabled = !(await isEnabled());
      if (stillDisabled) {
        // Permission denied, but don't force user to settings
        logger.warn("Location permission denied");
        return;
      }
    }
    
    const loc = await getCurrentLocation({ timeout: 20000 });
    userLocation.value = { lat: loc.latitude, lng: loc.longitude };
    
    // Only center once on initial load
    if (!hasCenteredInitially.value && mapInstance.value?.setCenter) {
      mapCenterLat.value = loc.latitude;
      mapCenterLng.value = loc.longitude;
      mapInstance.value.setCenter({
        lat: loc.latitude,
        lng: loc.longitude,
        animated: true
      });
      hasCenteredInitially.value = true;
    }
  } catch (e) {
    logger.error("Failed to get location:", e);
    // Don't show error - user can still tap to select location
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

async function useMyLocation() {
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
    // Don't auto-center - let user pan freely
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
      // Try different removal methods
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
        // Remove pickup marker when tapped
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
      // Try different removal methods
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
        // Remove dropoff marker when tapped
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
        const nextStyle = styles[1]; // Use "streets" as default
        
        // Change to different style
        mapbox.value.setMapStyle(nextStyle);
        setTimeout(() => {
          // Change back to original
          if (mapbox.value && typeof mapbox.value.setMapStyle === 'function') {
            mapbox.value.setMapStyle("streets");
            logger.log(`Style cycled: ${nextStyle} -> streets`);
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
          let currentZoomLevel = 13;
          
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
              const styleUri = `mapbox://styles/mapbox/streets-v11`;
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

// Handle tab activation to refresh map (fix black screen)
(globalThis as any).__homeTabActivated = () => {
  // Aggressive refresh when tab is activated (black screen fix)
  // Multiple refresh attempts with delays to ensure map recovers
  refreshMapVisibility();
  setTimeout(() => refreshMapVisibility(), 100);
  setTimeout(() => refreshMapVisibility(), 300);
};

// Handle app lifecycle events
let resumeHandler: any = null;
let pauseHandler: any = null;
let lowMemoryHandler: any = null;
let orientationHandler: any = null;
let pageLoadedHandler: any = null;
let visibilityCheckInterval: any = null;
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
    const timeout = setTimeout(() => {
      refreshMapVisibility();
      pendingTimeouts.delete(timeout);
    }, 200);
    pendingTimeouts.add(timeout);
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
              const timeout = setTimeout(() => {
                refreshMapVisibility();
                pendingTimeouts.delete(timeout);
              }, 300);
              pendingTimeouts.add(timeout);
            }
          } else {
            clearInterval(orientationCheckInterval);
          }
        }, 500);
        
        // Store interval for cleanup
        (orientationHandler as any) = orientationCheckInterval;
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
        const timeout = setTimeout(() => {
          refreshMapVisibility();
          pendingTimeouts.delete(timeout);
        }, 100);
        pendingTimeouts.add(timeout);
      });
    }
  }
});

onUnmounted(() => {
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
          const tempStyle = "streets"; // Use default
          mapbox.value.setMapStyle(tempStyle);
          setTimeout(() => {
            if (mapbox.value && typeof mapbox.value.setMapStyle === 'function') {
              mapbox.value.setMapStyle("streets");
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
              if (style.getSource && style.getLayer) {
                // Style is accessible, try to trigger a refresh
                if (nativeMap.triggerRepaint) {
                  nativeMap.triggerRepaint();
                }
              }
            }
          } catch (styleCheckError) {
            // Style might not be loaded - try to reload it
            try {
              const Style = (android as any).mapbox?.mapboxsdk?.maps?.Style;
              if (Style && nativeMap.setStyle) {
                const styleUri = `mapbox://styles/mapbox/streets-v11`;
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
</script>
