import { apiRequest } from "./api";

export function updateDriverLocation(lat: number, lng: number) {
  return apiRequest("POST", "/api/drivers/location/update", {
    latitude: lat,
    longitude: lng,
  });
}
