<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import api from '../services/axios'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import macedoniaGeoJson from '../assets/mk.json'

const mapContainer = ref(null)
const map = ref(null)
const markers = ref([])
const routes = ref([]) // Polylines for routes
const pickupMarkers = ref([])
const dropoffMarkers = ref([])
const agentMarkers = ref([]) // Moving agent markers
const agentAnimations = ref([]) // Animation state for each agent
const macedoniaBorder = ref(null) // Border polygon
const deliveries = ref([])
const simulatedDeliveries = ref([])
const loading = ref(false)
const error = ref(null)
const selectedDelivery = ref(null)
const useSimulation = ref(false)
const showSimulation = ref(false)
const animationInterval = ref(null)
const routeLoadingProgress = ref(0)
const isSimulating = ref(false)

// Macedonia bounds: roughly 40.8°-42.4° N, 20.4°-23.0° E
// Center: Skopje ~41.6086° N, 21.7453° E
const MACEDONIA_CENTER = [41.6086, 21.7453]
const MACEDONIA_BOUNDS = [
  [40.8, 20.4], // Southwest
  [42.4, 23.0]  // Northeast
]

// Extract border coordinates from GeoJSON and simplify for performance
// GeoJSON uses [longitude, latitude], Leaflet uses [latitude, longitude]
function getMacedoniaBorderCoords() {
  try {
    // Get the first feature (main country border)
    const feature = macedoniaGeoJson.features?.[0]
    if (!feature || !feature.geometry || feature.geometry.type !== 'Polygon') {
      console.warn('Invalid GeoJSON structure, using fallback')
      return null
    }
    
    // Extract coordinates - GeoJSON Polygon has array of rings, first ring is outer boundary
    let coordinates = feature.geometry.coordinates[0]
    
    // Simplify coordinates to reduce points for better performance
    // Keep every Nth point to reduce complexity while maintaining shape
    const simplifyFactor = Math.max(1, Math.floor(coordinates.length / 500)) // Target ~500 points max
    if (simplifyFactor > 1) {
      coordinates = coordinates.filter((_, index) => index % simplifyFactor === 0)
    }
    
    // Convert from [lng, lat] to [lat, lng] for Leaflet
    return coordinates.map(coord => [coord[1], coord[0]])
  } catch (e) {
    console.error('Error parsing Macedonia GeoJSON:', e)
    return null
  }
}

// Generate random coordinates within Macedonia bounds
function generateRandomLocation() {
  const lat = 40.8 + Math.random() * (42.4 - 40.8)
  const lng = 20.4 + Math.random() * (23.0 - 20.4)
  return [lat, lng]
}

// Major cities in Macedonia for more realistic simulation
const MACEDONIA_CITIES = [
  { name: 'Skopje', coords: [41.9973, 21.4280] },
  { name: 'Bitola', coords: [41.0311, 21.3347] },
  { name: 'Kumanovo', coords: [42.1322, 21.7144] },
  { name: 'Prilep', coords: [41.3444, 21.5528] },
  { name: 'Tetovo', coords: [42.0097, 20.9716] },
  { name: 'Veles', coords: [41.7156, 21.7753] },
  { name: 'Štip', coords: [41.7458, 22.1958] },
  { name: 'Ohrid', coords: [41.1231, 20.8016] },
  { name: 'Gostivar', coords: [41.7972, 20.9069] },
  { name: 'Strumica', coords: [41.4375, 22.6433] },
  { name: 'Kavadarci', coords: [41.4331, 22.0119] },
  { name: 'Kočani', coords: [41.9167, 22.4125] },
  { name: 'Kičevo', coords: [41.5125, 20.9583] },
  { name: 'Struga', coords: [41.1775, 20.6783] },
  { name: 'Radoviš', coords: [41.6383, 22.4647] },
]

function getRandomCityLocation() {
  const city = MACEDONIA_CITIES[Math.floor(Math.random() * MACEDONIA_CITIES.length)]
  // Add some random offset around the city (within ~10km)
  const offsetLat = (Math.random() - 0.5) * 0.15
  const offsetLng = (Math.random() - 0.5) * 0.15
  return [city.coords[0] + offsetLat, city.coords[1] + offsetLng]
}

// Sample delivery titles for simulation
const SAMPLE_TITLES = [
  'Food Delivery',
  'Package Delivery',
  'Express Delivery',
  'Document Delivery',
  'Grocery Delivery',
  'Medicine Delivery',
  'Electronics Delivery',
  'Clothing Delivery',
  'Book Delivery',
  'Flower Delivery',
]

const STATUSES = ['PENDING', 'ASSIGNED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED']

async function generateSimulatedDeliveries(count = 7) {
  const simulated = []
  const usedCities = new Set()
  
  for (let i = 0; i < count; i++) {
    // Update progress
    routeLoadingProgress.value = Math.round(((i + 1) / count) * 100)
    // Pick two different cities for pickup and dropoff
    let pickupCity, dropoffCity
    if (usedCities.size < MACEDONIA_CITIES.length) {
      const availableCities = MACEDONIA_CITIES.filter(c => !usedCities.has(c.name))
      pickupCity = availableCities[Math.floor(Math.random() * availableCities.length)]
      usedCities.add(pickupCity.name)
      const remainingCities = MACEDONIA_CITIES.filter(c => c.name !== pickupCity.name)
      dropoffCity = remainingCities[Math.floor(Math.random() * remainingCities.length)]
    } else {
      pickupCity = MACEDONIA_CITIES[Math.floor(Math.random() * MACEDONIA_CITIES.length)]
      const otherCities = MACEDONIA_CITIES.filter(c => c.name !== pickupCity.name)
      dropoffCity = otherCities[Math.floor(Math.random() * otherCities.length)]
    }
    
    const pickupCoords = [
      pickupCity.coords[0] + (Math.random() - 0.5) * 0.1,
      pickupCity.coords[1] + (Math.random() - 0.5) * 0.1
    ]
    const dropoffCoords = [
      dropoffCity.coords[0] + (Math.random() - 0.5) * 0.1,
      dropoffCity.coords[1] + (Math.random() - 0.5) * 0.1
    ]
    
    // Fetch actual road route
    const routePath = await fetchRoute(pickupCoords, dropoffCoords)
    
    // Determine status and progress
    const statusOptions = ['ASSIGNED', 'IN_TRANSIT', 'IN_TRANSIT', 'IN_TRANSIT', 'DELIVERED']
    const status = statusOptions[Math.floor(Math.random() * statusOptions.length)]
    const title = SAMPLE_TITLES[Math.floor(Math.random() * SAMPLE_TITLES.length)]
    
    // Random progress (0 = at pickup, 1 = at dropoff)
    const progress = status === 'DELIVERED' ? 1 : status === 'ASSIGNED' ? 0 : Math.random()
    
    // Get current position along the route
    const currentCoords = getPointAlongRoute(routePath, progress) || pickupCoords
    
    simulated.push({
      id: `SIM-${1000 + i}`,
      title: `${title} - ${dropoffCity.name}`,
      description: `Simulated delivery from ${pickupCity.name} to ${dropoffCity.name}`,
      status: status,
      pickupAddress: `Pickup: ${pickupCity.name}`,
      dropoffAddress: `Dropoff: ${dropoffCity.name}`,
      createdAt: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      pickupCoords: pickupCoords,
      dropoffCoords: dropoffCoords,
      routePath: routePath, // Store the full route path
      currentCoords: currentCoords,
      progress: progress,
      isSimulated: true,
    })
  }
  
  return simulated
}

// Interpolate between two coordinates
function interpolateCoords(start, end, t) {
  return [
    start[0] + (end[0] - start[0]) * t,
    start[1] + (end[1] - start[1]) * t
  ]
}

// Fetch actual road route using OSRM (Open Source Routing Machine)
async function fetchRoute(start, end) {
  try {
    // OSRM format: [lng, lat] (note: longitude first!)
    const startLngLat = `${start[1]},${start[0]}`
    const endLngLat = `${end[1]},${end[0]}`
    
    // Use OSRM demo server (free, no API key needed)
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000) // 5 second timeout
    
    try {
      const response = await fetch(
        `https://router.project-osrm.org/route/v1/driving/${startLngLat};${endLngLat}?overview=full&geometries=geojson`,
        { signal: controller.signal }
      )
      
      clearTimeout(timeoutId)
      
      if (!response.ok) {
        throw new Error(`OSRM API returned ${response.status}`)
      }
      
      const data = await response.json()
      
      if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
        // Convert GeoJSON coordinates [lng, lat] to Leaflet format [lat, lng]
        const routeCoordinates = data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]])
        return routeCoordinates
      }
    } catch (fetchError) {
      clearTimeout(timeoutId)
      throw fetchError
    }
  } catch (error) {
    console.warn('Failed to fetch route from OSRM, using straight line:', error)
  }
  
  // Fallback to straight line if routing fails
  return [start, end]
}

// Get point along a route path at a given progress (0-1)
function getPointAlongRoute(routePath, progress) {
  if (!routePath || routePath.length === 0) return null
  if (progress <= 0) return routePath[0]
  if (progress >= 1) return routePath[routePath.length - 1]
  
  const totalLength = routePath.length - 1
  const exactIndex = progress * totalLength
  const index = Math.floor(exactIndex)
  const fraction = exactIndex - index
  
  if (index >= routePath.length - 1) {
    return routePath[routePath.length - 1]
  }
  
  // Interpolate between two points on the route
  const point1 = routePath[index]
  const point2 = routePath[index + 1]
  
  return [
    point1[0] + (point2[0] - point1[0]) * fraction,
    point1[1] + (point2[1] - point1[1]) * fraction
  ]
}

// Create icons (moved to top level for accessibility)
function createIcon(color, size = 24, label = '', isSelected = false) {
  const borderColor = isSelected ? '#00ffff' : 'white'
  const borderWidth = isSelected ? 4 : 2
  const shadow = isSelected ? '0 0 10px rgba(0, 255, 255, 0.8), 0 2px 4px rgba(0,0,0,0.5)' : '0 2px 4px rgba(0,0,0,0.5)'
  
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="
      width: ${size}px;
      height: ${size}px;
      background: ${color};
      border: ${borderWidth}px solid ${borderColor};
      border-radius: 50%;
      box-shadow: ${shadow};
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
      font-weight: bold;
      color: ${color === '#ffffff' || color === '#cccccc' ? '#000' : '#fff'};
    ">${label}</div>`,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  })
}

// Function to update agent marker icon based on selection (moved to top level)
function updateAgentMarkerIcon(marker, delivery, isSelected) {
  if (!marker) return
  const newIcon = createIcon('#ffff00', 28, '🚚', isSelected)
  marker.setIcon(newIcon)
}

async function loadSimulatedDeliveries() {
  // Prevent double loading
  if (isSimulating.value || loading.value) {
    return
  }
  
  // Clear existing animations
  stopAnimations()
  
  isSimulating.value = true
  loading.value = true
  error.value = null
  routeLoadingProgress.value = 0
  
  try {
    // Ensure map is ready before generating deliveries
    const isReady = await ensureMapReady()
    if (!isReady) {
      throw new Error('Map not ready. Please wait for the map to load.')
    }
    
    simulatedDeliveries.value = await generateSimulatedDeliveries(7)
    routeLoadingProgress.value = 100
    await updateMapWithRoutes()
    startAnimations()
  } catch (e) {
    error.value = e?.message || 'Failed to load simulated deliveries'
    console.error('Error loading simulated deliveries:', e)
  } finally {
    loading.value = false
    isSimulating.value = false
    routeLoadingProgress.value = 0
  }
}

async function fetchDeliveries() {
  // Don't auto-load simulation if user hasn't clicked simulate
  if (isSimulating.value) return
  
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/api/admin/deliveries', { params: { page: 0, size: 50 } })
    const data = res.data
    const allDeliveries = Array.isArray(data) ? data : data?.content ?? data?.items ?? []
    
    // Limit to 15 deliveries for simulation
    deliveries.value = allDeliveries.slice(0, 15)
    
    // Only auto-show simulation if we're already in simulation mode
    if (deliveries.value.length === 0 && useSimulation.value) {
      await loadSimulatedDeliveries()
      return
    }
    
    // If no deliveries and not in simulation mode, just show empty
    if (deliveries.value.length === 0) {
      return
    }
    
    // Fetch tracking data for each delivery to get coordinates
    await loadDeliveryLocations()
  } catch (e) {
    // Don't show error for 403/401 - these are handled by auth interceptor
    const status = e?.response?.status
    if (status === 403 || status === 401) {
      // Auth error - will be handled by router guard
      error.value = null
    } else {
      error.value = e?.response?.data?.message || e?.message || 'Failed to load deliveries'
    }
    deliveries.value = []
    // Only auto-show simulation if we're already in simulation mode
    if (useSimulation.value) {
      await loadSimulatedDeliveries()
    }
  } finally {
    loading.value = false
  }
}

async function toggleSimulation() {
  // Prevent double-clicking
  if (loading.value || isSimulating.value) {
    return
  }
  
  stopAnimations()
  showSimulation.value = !showSimulation.value
  useSimulation.value = showSimulation.value
  
  if (showSimulation.value) {
    await loadSimulatedDeliveries()
  } else {
    // Reload real deliveries
    if (deliveries.value.length > 0) {
      await loadDeliveryLocations()
    } else {
      await fetchDeliveries()
    }
  }
}

async function loadDeliveryLocations() {
  stopAnimations()
  
  // Try to get coordinates from tracking data for real deliveries
  for (const delivery of deliveries.value) {
    let coords = null
    
    // Try to get coordinates from tracking data
    try {
      const trackingRes = await api.get(`/api/tracking/${delivery.id}`)
      const tracking = trackingRes.data
      
      // Check various possible location fields
      if (tracking?.currentLocation?.latitude && tracking?.currentLocation?.longitude) {
        coords = [tracking.currentLocation.latitude, tracking.currentLocation.longitude]
      } else if (tracking?.location?.lat && tracking?.location?.lng) {
        coords = [tracking.location.lat, tracking.location.lng]
      } else if (tracking?.lat && tracking?.lng) {
        coords = [tracking.lat, tracking.lng]
      } else if (tracking?.coordinates) {
        coords = Array.isArray(tracking.coordinates) ? tracking.coordinates : [tracking.coordinates.lat, tracking.coordinates.lng]
      }
    } catch (e) {
      // Tracking data not available, will use random location
    }
    
    // If no coordinates found, generate simulated location
    if (!coords) {
      coords = getRandomCityLocation()
    }
    
    // Store coordinates in delivery object
    delivery.currentCoords = coords
  }
  
  await updateMapWithRoutes()
}

function clearMap() {
  // Clear all markers and routes (but keep border)
  if (!map.value) return
  
  markers.value.forEach(marker => {
    if (map.value.hasLayer(marker)) {
      map.value.removeLayer(marker)
    }
  })
  routes.value.forEach(route => {
    if (map.value.hasLayer(route)) {
      map.value.removeLayer(route)
    }
  })
  pickupMarkers.value.forEach(marker => {
    if (map.value.hasLayer(marker)) {
      map.value.removeLayer(marker)
    }
  })
  dropoffMarkers.value.forEach(marker => {
    if (map.value.hasLayer(marker)) {
      map.value.removeLayer(marker)
    }
  })
  agentMarkers.value.forEach(marker => {
    if (map.value.hasLayer(marker)) {
      map.value.removeLayer(marker)
    }
  })
  
  markers.value = []
  routes.value = []
  pickupMarkers.value = []
  dropoffMarkers.value = []
  agentMarkers.value = []
}

// Generate a single new delivery
async function generateSingleDelivery() {
  // Pick two different cities for pickup and dropoff
  const pickupCity = MACEDONIA_CITIES[Math.floor(Math.random() * MACEDONIA_CITIES.length)]
  const otherCities = MACEDONIA_CITIES.filter(c => c.name !== pickupCity.name)
  const dropoffCity = otherCities[Math.floor(Math.random() * otherCities.length)]
  
  const pickupCoords = [
    pickupCity.coords[0] + (Math.random() - 0.5) * 0.1,
    pickupCity.coords[1] + (Math.random() - 0.5) * 0.1
  ]
  const dropoffCoords = [
    dropoffCity.coords[0] + (Math.random() - 0.5) * 0.1,
    dropoffCity.coords[1] + (Math.random() - 0.5) * 0.1
  ]
  
  // Fetch actual road route
  const routePath = await fetchRoute(pickupCoords, dropoffCoords)
  
  const status = 'ASSIGNED'
  const title = SAMPLE_TITLES[Math.floor(Math.random() * SAMPLE_TITLES.length)]
  const progress = 0
  
  const currentCoords = pickupCoords
  
  return {
    id: `SIM-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    title: `${title} - ${dropoffCity.name}`,
    description: `Simulated delivery from ${pickupCity.name} to ${dropoffCity.name}`,
    status: status,
    pickupAddress: `Pickup: ${pickupCity.name}`,
    dropoffAddress: `Dropoff: ${dropoffCity.name}`,
    createdAt: new Date().toISOString(),
    pickupCoords: pickupCoords,
    dropoffCoords: dropoffCoords,
    routePath: routePath,
    currentCoords: currentCoords,
    progress: progress,
    isSimulated: true,
  }
}

async function updateMapWithRoutes() {
  clearMap()
  
  // Ensure map is ready before adding layers
  const isReady = await ensureMapReady()
  if (!isReady || !map.value) {
    console.warn('Map not ready, skipping update')
    return
  }
  
  const isSimulated = showSimulation.value || useSimulation.value
  const deliveriesToShow = isSimulated ? simulatedDeliveries.value : deliveries.value
  
  if (deliveriesToShow.length === 0) return
  
  // Use the top-level createIcon function
  
  const allBounds = []
  
  deliveriesToShow.forEach((delivery) => {
    const isSim = delivery.isSimulated || delivery.id?.toString().startsWith('SIM-')
    
    if (isSim && delivery.pickupCoords && delivery.dropoffCoords) {
      // Simulated delivery with route
      const pickup = delivery.pickupCoords
      const dropoff = delivery.dropoffCoords
      const current = delivery.currentCoords || pickup
      
      // Create route polyline using actual route path if available
      const routeCoordinates = delivery.routePath || [pickup, dropoff]
      const route = L.polyline(routeCoordinates, {
        color: '#666666',
        weight: 3,
        opacity: 0.7,
        dashArray: '8, 4',
      }).addTo(map.value)
      routes.value.push(route)
      
      // Pickup marker
      const pickupMarker = L.marker(pickup, {
        icon: createIcon('#00ff00', 20, 'P'),
      }).addTo(map.value)
      pickupMarker.bindPopup(`<div style="color: white;"><strong>Pickup</strong><br>${delivery.pickupAddress || 'Pickup location'}</div>`)
      pickupMarkers.value.push(pickupMarker)
      
      // Dropoff marker
      const dropoffMarker = L.marker(dropoff, {
        icon: createIcon('#ff0000', 20, 'D'),
      }).addTo(map.value)
      dropoffMarker.bindPopup(`<div style="color: white;"><strong>Dropoff</strong><br>${delivery.dropoffAddress || 'Dropoff location'}</div>`)
      dropoffMarkers.value.push(dropoffMarker)
      
      // Agent marker (moving)
      const isSelected = selectedDelivery.value?.id === delivery.id
      const agentIcon = createIcon('#ffff00', 28, '🚚', isSelected)
      const agentMarker = L.marker(current, {
        icon: agentIcon,
      }).addTo(map.value)
      
      // No popup - just selection on click
      agentMarker.on('click', (e) => {
        if (e.originalEvent) {
          e.originalEvent.stopPropagation()
          e.originalEvent.preventDefault()
        }
        
        // Find the actual delivery object from simulatedDeliveries to ensure reactivity
        const actualDelivery = simulatedDeliveries.value.find(d => d.id === delivery.id) || delivery
        
        // Update all agent markers to remove selection
        agentMarkers.value.forEach((marker) => {
          const anim = agentAnimations.value.find(a => a.marker === marker)
          if (anim) {
            const del = simulatedDeliveries.value.find(d => d.id === anim.deliveryId)
            if (del) {
              updateAgentMarkerIcon(marker, del, false)
            }
          }
        })
        
        // Set selected delivery - this should trigger the sidebar to appear
        // Use a fresh object reference to ensure Vue reactivity
        selectedDelivery.value = { ...actualDelivery }
        
        // Debug log
        
        // Update clicked marker to show selection
        updateAgentMarkerIcon(agentMarker, actualDelivery, true)
      })
      
      // Also make marker interactive
      agentMarker.options.interactive = true
      agentMarkers.value.push(agentMarker)
      
      allBounds.push(pickup, dropoff, current)
      
      // Store animation state with route path and route reference
      agentAnimations.value.push({
        deliveryId: delivery.id,
        pickup: pickup,
        dropoff: dropoff,
        routePath: delivery.routePath || [pickup, dropoff], // Store the route path
        routePolyline: route, // Store reference to the route polyline
        pickupMarker: pickupMarker, // Store reference to pickup marker
        dropoffMarker: dropoffMarker, // Store reference to dropoff marker
        progress: delivery.progress || 0,
        direction: delivery.status === 'DELIVERED' ? 0 : 1, // 1 = going to dropoff, 0 = stopped
        speed: 0.001 + Math.random() * 0.002, // Speed of movement (slower for route following)
        marker: agentMarker,
      })
    } else {
      // Regular delivery (no route simulation)
      const coords = delivery.currentCoords || delivery.coords || getRandomCityLocation()
      const status = delivery.status || 'PENDING'
      const statusColors = {
        PENDING: '#ffffff',
        ASSIGNED: '#cccccc',
        IN_TRANSIT: '#999999',
        DELIVERED: '#000000',
        CANCELLED: '#666666',
      }
      const color = statusColors[status] || '#ffffff'
      
      const marker = L.marker(coords, {
        icon: createIcon(color),
      })
      
      const popupContent = `
        <div style="color: white; min-width: 200px;">
          <div style="font-weight: bold; margin-bottom: 8px;">Delivery #${delivery.id}</div>
          <div style="font-size: 12px; margin-bottom: 4px;"><strong>Status:</strong> ${status}</div>
          <div style="font-size: 12px; margin-bottom: 4px;"><strong>Title:</strong> ${delivery.title || delivery.description || '—'}</div>
          ${delivery.pickupAddress ? `<div style="font-size: 11px; color: #ccc; margin-top: 4px;">From: ${delivery.pickupAddress}</div>` : ''}
          ${delivery.dropoffAddress ? `<div style="font-size: 11px; color: #ccc;">To: ${delivery.dropoffAddress}</div>` : ''}
        </div>
      `
      
      marker.bindPopup(popupContent)
      marker.on('click', () => {
        selectedDelivery.value = delivery
      })
      marker.addTo(map.value)
      markers.value.push(marker)
      allBounds.push(coords)
    }
  })
  
  // Fit map to show all markers
  if (allBounds.length > 0) {
    const bounds = L.latLngBounds(allBounds)
    map.value.fitBounds(bounds.pad(0.1))
  }
}

async function replaceCompletedDelivery(deliveryId) {
  // Find and remove the completed delivery
  const index = simulatedDeliveries.value.findIndex(d => d.id === deliveryId)
  if (index === -1) return
  
  // Remove from animations
  const animIndex = agentAnimations.value.findIndex(a => a.deliveryId === deliveryId)
  if (animIndex !== -1) {
    const anim = agentAnimations.value[animIndex]
    
    // Remove all markers and route directly using stored references
    if (anim.marker) {
      map.value.removeLayer(anim.marker)
      const agentIndex = agentMarkers.value.indexOf(anim.marker)
      if (agentIndex !== -1) agentMarkers.value.splice(agentIndex, 1)
    }
    
    // Remove route polyline
    if (anim.routePolyline) {
      map.value.removeLayer(anim.routePolyline)
      const routeIndex = routes.value.indexOf(anim.routePolyline)
      if (routeIndex !== -1) routes.value.splice(routeIndex, 1)
    }
    
    // Remove pickup marker
    if (anim.pickupMarker) {
      map.value.removeLayer(anim.pickupMarker)
      const pickupIndex = pickupMarkers.value.indexOf(anim.pickupMarker)
      if (pickupIndex !== -1) pickupMarkers.value.splice(pickupIndex, 1)
    }
    
    // Remove dropoff marker
    if (anim.dropoffMarker) {
      map.value.removeLayer(anim.dropoffMarker)
      const dropoffIndex = dropoffMarkers.value.indexOf(anim.dropoffMarker)
      if (dropoffIndex !== -1) dropoffMarkers.value.splice(dropoffIndex, 1)
    }
    
    agentAnimations.value.splice(animIndex, 1)
  }
  
  // Generate new delivery
  try {
    const newDelivery = await generateSingleDelivery()
    simulatedDeliveries.value[index] = newDelivery
    
    // Add new delivery to map
    const pickup = newDelivery.pickupCoords
    const dropoff = newDelivery.dropoffCoords
    const current = newDelivery.currentCoords
    
    // Create route
    const route = L.polyline(newDelivery.routePath, {
      color: '#666666',
      weight: 3,
      opacity: 0.7,
      dashArray: '8, 4',
    }).addTo(map.value)
    routes.value.push(route)
    
    // Use the top-level createIcon and updateAgentMarkerIcon functions
    
    const pickupMarker = L.marker(pickup, {
      icon: createIcon('#00ff00', 20, 'P'),
    }).addTo(map.value)
    pickupMarker.bindPopup(`<div style="color: white;"><strong>Pickup</strong><br>${newDelivery.pickupAddress}</div>`)
    pickupMarkers.value.push(pickupMarker)
    
    const dropoffMarker = L.marker(dropoff, {
      icon: createIcon('#ff0000', 20, 'D'),
    }).addTo(map.value)
    dropoffMarker.bindPopup(`<div style="color: white;"><strong>Dropoff</strong><br>${newDelivery.dropoffAddress}</div>`)
    dropoffMarkers.value.push(dropoffMarker)
    
    const isSelected = selectedDelivery.value?.id === newDelivery.id
    const agentIcon = createIcon('#ffff00', 28, '🚚', isSelected)
    const agentMarker = L.marker(current, {
      icon: agentIcon,
    }).addTo(map.value)
    
    // No popup - just selection on click
    agentMarker.on('click', (e) => {
      if (e.originalEvent) {
        e.originalEvent.stopPropagation()
        e.originalEvent.preventDefault()
      }
      
      // Find the actual delivery object from simulatedDeliveries to ensure reactivity
      const actualDelivery = simulatedDeliveries.value.find(d => d.id === newDelivery.id) || newDelivery
      
      // Update all agent markers to remove selection
      agentMarkers.value.forEach((marker) => {
        const anim = agentAnimations.value.find(a => a.marker === marker)
        if (anim) {
          const del = simulatedDeliveries.value.find(d => d.id === anim.deliveryId)
          if (del) {
            updateAgentMarkerIcon(marker, del, false)
          }
        }
      })
      
      // Set selected delivery - this should trigger the sidebar to appear
      // Use a fresh object reference to ensure Vue reactivity
      selectedDelivery.value = { ...actualDelivery }
      
      // Debug log
      console.log('Selected delivery:', selectedDelivery.value?.id, selectedDelivery.value)
      
      // Update clicked marker to show selection
      updateAgentMarkerIcon(agentMarker, actualDelivery, true)
    })
    
    // Also make marker interactive
    agentMarker.options.interactive = true
    agentMarkers.value.push(agentMarker)
    
    // Add to animations with all references
    agentAnimations.value.push({
      deliveryId: newDelivery.id,
      pickup: pickup,
      dropoff: dropoff,
      routePath: newDelivery.routePath,
      routePolyline: route, // Store reference to route
      pickupMarker: pickupMarker, // Store reference to pickup marker
      dropoffMarker: dropoffMarker, // Store reference to dropoff marker
      progress: 0,
      direction: 1,
      speed: 0.001 + Math.random() * 0.002,
      marker: agentMarker,
    })
  } catch (e) {
    console.error('Error replacing delivery:', e)
  }
}

function startAnimations() {
  if (animationInterval.value) return
  
  animationInterval.value = setInterval(async () => {
    for (let i = 0; i < agentAnimations.value.length; i++) {
      const anim = agentAnimations.value[i]
      if (anim.deliveryId.toString().startsWith('SIM-')) {
        // Check if delivery is completed
        const delivery = simulatedDeliveries.value.find(d => d.id === anim.deliveryId)
        if (delivery && anim.progress >= 1 && delivery.status === 'DELIVERED') {
          // Wait a bit before replacing (2 seconds)
          if (!anim.completedAt) {
            anim.completedAt = Date.now()
          } else if (Date.now() - anim.completedAt > 2000) {
            // Replace with new delivery
            await replaceCompletedDelivery(anim.deliveryId)
            continue
          }
        }
        
        // Update progress
        if (anim.progress < 1) {
          anim.progress += anim.speed * anim.direction
          
          // Stop at boundaries
          if (anim.progress >= 1) {
            anim.progress = 1
            anim.direction = 0 // Stop at dropoff
          } else if (anim.progress <= 0) {
            anim.progress = 0
            anim.direction = 1 // Start going to dropoff
          }
          
          // Get position along the actual route path
          const newCoords = getPointAlongRoute(anim.routePath, anim.progress)
          if (newCoords) {
            anim.marker.setLatLng(newCoords)
          }
          
          // Update delivery progress
          if (delivery) {
            if (newCoords) {
              delivery.currentCoords = newCoords
            }
            delivery.progress = anim.progress
            if (anim.progress >= 1 && delivery.status !== 'DELIVERED') {
              delivery.status = 'DELIVERED'
            } else if (anim.progress > 0 && anim.progress < 1 && delivery.status === 'ASSIGNED') {
              delivery.status = 'IN_TRANSIT'
            }
          }
        }
      }
    }
  }, 100) // Update every 100ms for smooth animation
}

function stopAnimations() {
  if (animationInterval.value) {
    clearInterval(animationInterval.value)
    animationInterval.value = null
  }
  agentAnimations.value = []
}

function updateMapMarkers(deliveryLocations) {
  // For non-simulated deliveries, use simple markers
  clearMap()
  updateMapWithRoutes()
}

function initMap() {
  if (!mapContainer.value) return
  
  // If map already exists, remove it first
  if (map.value) {
    try {
      map.value.remove()
    } catch (e) {
      // Ignore errors when removing
    }
    map.value = null
  }
  
  // Initialize map with dark theme
  map.value = L.map(mapContainer.value, {
    center: MACEDONIA_CENTER,
    zoom: 8,
    minZoom: 7,
    maxBounds: MACEDONIA_BOUNDS,
  })
  
  // Use OpenStreetMap tiles with dark theme (CartoDB Dark Matter)
  L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
    subdomains: 'abcd',
    maxZoom: 19,
  }).addTo(map.value)
  
  // Add Macedonia border from GeoJSON
  const borderCoords = getMacedoniaBorderCoords()
  if (borderCoords && borderCoords.length > 0) {
    macedoniaBorder.value = L.polygon(borderCoords, {
      color: '#ffffff',
      weight: 5,
      opacity: 1.0,
      fillColor: 'transparent',
      fillOpacity: 0,
      dashArray: '12, 6',
      smoothFactor: 1.0, // Reduce smoothing for better performance
      noClip: false, // Enable clipping for better performance
      updateWhileZooming: false, // Don't update while zooming
      updateWhileMoving: false, // Don't update while panning
    }).addTo(map.value)
  } else {
    // Fallback to simple border if GeoJSON fails
    console.warn('Using fallback border coordinates')
    const fallbackCoords = [
      [42.3736, 20.4529], [42.3000, 20.5000], [42.2000, 20.6000],
      [41.9000, 20.8000], [41.5000, 21.2500], [40.8500, 22.4500],
      [41.0500, 23.0000], [42.3500, 22.3000], [42.3736, 20.4529]
    ]
    macedoniaBorder.value = L.polygon(fallbackCoords, {
      color: '#ffffff',
      weight: 5,
      opacity: 1.0,
      fillColor: 'transparent',
      fillOpacity: 0,
      dashArray: '12, 6',
    }).addTo(map.value)
  }
}

// Ensure map is ready before adding layers
function ensureMapReady() {
  return new Promise((resolve) => {
    if (!mapContainer.value) {
      resolve(false)
      return
    }
    
    // If map doesn't exist, initialize it
    if (!map.value) {
      initMap()
    }
    
    // Wait for map to be ready
    if (map.value) {
      // Check if map container is in DOM
      if (!mapContainer.value || !mapContainer.value.parentElement) {
        resolve(false)
        return
      }
      
      // Use whenReady to ensure map is fully initialized
      map.value.whenReady(() => {
        // Double-check map is still valid
        if (map.value && map.value.getContainer() && map.value.getContainer().parentElement) {
          resolve(true)
        } else {
          resolve(false)
        }
      })
    } else {
      resolve(false)
    }
  })
}

onMounted(async () => {
  // Wait for next tick to ensure DOM is ready
  await nextTick()
  initMap()
  // Wait for map to be ready before fetching deliveries
  await ensureMapReady()
  await fetchDeliveries()
  // Don't auto-load simulation on mount - let user click the button
})

// Watch for selectedDelivery changes to update marker borders
watch(selectedDelivery, (newSelection, oldSelection) => {
  // Update all agent markers based on selection
  agentMarkers.value.forEach((marker) => {
    const anim = agentAnimations.value.find(a => a.marker === marker)
    if (anim) {
      const delivery = simulatedDeliveries.value.find(d => d.id === anim.deliveryId)
      if (delivery) {
        const isSelected = newSelection?.id === delivery.id
        updateAgentMarkerIcon(marker, delivery, isSelected)
      }
    }
  })
})

onUnmounted(() => {
  stopAnimations()
  if (map.value) {
    map.value.remove()
  }
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold mb-1">Delivery Tracking Map</h1>
        <p class="text-sm text-neutral-400">
          {{ showSimulation || useSimulation ? 'Simulated delivery locations across Macedonia' : 'Real-time delivery locations across Macedonia' }}
        </p>
      </div>
      <div class="flex items-center gap-2">
        <button 
          class="btn" 
          :class="showSimulation ? 'btn-primary' : 'btn-secondary'"
          @click="toggleSimulation"
          :disabled="loading"
        >
          {{ loading ? 'Loading...' : (showSimulation ? 'Show Real Data' : 'Simulate Deliveries') }}
        </button>
        <button class="btn btn-secondary" @click="fetchDeliveries" :disabled="loading">
          {{ loading ? 'Loading...' : 'Refresh' }}
        </button>
      </div>
    </div>

    <div v-if="error && !useSimulation" class="p-4 bg-red-950/20 border border-red-900/50 text-red-300 rounded text-sm">
      {{ error }}
    </div>

    <div v-if="showSimulation || useSimulation" class="p-4 bg-blue-950/20 border border-blue-900/50 text-blue-300 rounded text-sm">
      <strong>Simulation Mode:</strong> Showing {{ simulatedDeliveries.length }} simulated deliveries across Macedonia. 
      {{ deliveries.length > 0 ? 'Click "Show Real Data" to view actual deliveries.' : 'No real delivery data available.' }}
    </div>

    <!-- Loading Progress Bar -->
    <div v-if="loading && routeLoadingProgress > 0" class="card">
      <div class="flex items-center justify-between mb-2">
        <span class="text-sm text-neutral-400">Loading routes...</span>
        <span class="text-sm text-neutral-400">{{ routeLoadingProgress }}%</span>
      </div>
      <div class="w-full bg-neutral-800 rounded-full h-2">
        <div 
          class="bg-white h-2 rounded-full transition-all duration-300"
          :style="{ width: `${routeLoadingProgress}%` }"
        ></div>
      </div>
    </div>

    <div class="card p-0 overflow-hidden relative">
      <div ref="mapContainer" class="w-full h-[600px] bg-neutral-900"></div>
      
      <!-- Selected Delivery Details - Right Side Overlay -->
      <Transition name="slide">
        <div 
          v-if="selectedDelivery && selectedDelivery.id" 
          key="delivery-sidebar"
          class="absolute top-4 right-4 w-80 bg-neutral-900 border-2 border-neutral-700 rounded-lg shadow-2xl z-[2000] max-h-[calc(100%-2rem)] overflow-y-auto"
          style="pointer-events: auto;"
        >
        <div class="p-4 space-y-3">
          <div class="flex items-center justify-between mb-2">
            <div class="font-semibold text-white">Delivery #{{ selectedDelivery.id }}</div>
            <button 
              class="text-neutral-400 hover:text-white transition-colors text-xl leading-none" 
              @click="selectedDelivery = null"
              title="Close"
            >
              ×
            </button>
          </div>
          <div class="space-y-2 text-sm text-neutral-300">
            <div>
              <span class="text-neutral-400">Status:</span>
              <span class="ml-2 font-medium">{{ selectedDelivery.status }}</span>
            </div>
            <div>
              <span class="text-neutral-400">Title:</span>
              <span class="ml-2">{{ selectedDelivery.title || selectedDelivery.description || '—' }}</span>
            </div>
            <div v-if="selectedDelivery.pickupAddress">
              <span class="text-neutral-400">Pickup:</span>
              <span class="ml-2">{{ selectedDelivery.pickupAddress }}</span>
            </div>
            <div v-if="selectedDelivery.dropoffAddress">
              <span class="text-neutral-400">Dropoff:</span>
              <span class="ml-2">{{ selectedDelivery.dropoffAddress }}</span>
            </div>
            <div v-if="selectedDelivery.createdAt">
              <span class="text-neutral-400">Created:</span>
              <span class="ml-2">{{ new Date(selectedDelivery.createdAt).toLocaleString() }}</span>
            </div>
            <div v-if="selectedDelivery.isSimulated && selectedDelivery.progress !== undefined" class="mt-4 pt-3 border-t border-neutral-800">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs text-neutral-400">Route Progress</span>
                <span class="text-xs text-neutral-400 font-medium">{{ Math.round(selectedDelivery.progress * 100) }}%</span>
              </div>
              <div class="w-full bg-neutral-800 rounded-full h-2">
                <div 
                  class="bg-white h-2 rounded-full transition-all duration-300"
                  :style="{ width: `${selectedDelivery.progress * 100}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>
        </div>
      </Transition>
    </div>

    <!-- Legend -->
    <div class="card">
      <div class="text-sm font-semibold mb-3">Map Legend</div>
      <div class="space-y-3">
        <div>
          <div class="text-xs font-semibold mb-2 text-neutral-400">Route Markers (Simulation)</div>
          <div class="flex flex-wrap gap-4 text-xs">
            <div class="flex items-center gap-2">
              <div class="w-5 h-5 rounded-full bg-green-500 border-2 border-white flex items-center justify-center text-[10px] font-bold text-black">P</div>
              <span>Pickup</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-5 h-5 rounded-full bg-yellow-400 border-2 border-white flex items-center justify-center text-[10px]">🚚</div>
              <span>Agent (Moving)</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-5 h-5 rounded-full bg-red-500 border-2 border-white flex items-center justify-center text-[10px] font-bold text-white">D</div>
              <span>Dropoff</span>
            </div>
          </div>
        </div>
        <div class="pt-2 border-t border-neutral-800">
          <div class="text-xs font-semibold mb-2 text-neutral-400">Status Colors</div>
          <div class="flex flex-wrap gap-4 text-xs">
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 rounded-full bg-white border-2 border-white"></div>
              <span>PENDING</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 rounded-full bg-neutral-300 border-2 border-white"></div>
              <span>ASSIGNED</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 rounded-full bg-neutral-500 border-2 border-white"></div>
              <span>IN_TRANSIT</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 rounded-full bg-black border-2 border-white"></div>
              <span>DELIVERED</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 rounded-full bg-neutral-600 border-2 border-white"></div>
              <span>CANCELLED</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.leaflet-popup-content-wrapper) {
  background: #1a1a1a;
  color: white;
  border-radius: 8px;
}

:deep(.leaflet-popup-tip) {
  background: #1a1a1a;
}

:deep(.leaflet-popup-close-button) {
  color: white;
}

:deep(.leaflet-container) {
  background: #0a0a0a;
}

/* Slide transition for sidebar */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>