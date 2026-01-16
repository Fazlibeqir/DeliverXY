import { apiRequest } from "./api";
import { debounce } from "../utils/debounce";

// Debounced location update to prevent excessive API calls
// Updates are debounced by 2 seconds (location updates come every 2 seconds from GPS)
const debouncedUpdate = debounce(
  async (lat: number, lng: number) => {
    try {
      await apiRequest("POST", "/api/drivers/location/update", {
        latitude: lat,
        longitude: lng,
      });
    } catch (error) {
      // Silently fail - location updates are not critical
      // User can still use the app if location update fails
    }
  },
  2000 // 2 second debounce
);

export function updateDriverLocation(lat: number, lng: number) {
  // Use debounced version to prevent excessive API calls
  debouncedUpdate(lat, lng);
}
