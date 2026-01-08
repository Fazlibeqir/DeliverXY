import { apiRequest } from "./api";

export type VehicleRequest = {
  vehicleType: string;
  make: string;
  model: string;
  vehicleYear?: number;
  licensePlate: string;
  color?: string;
  passengerCapacity?: number;
  cargoCapacityKg?: number;
  cargoVolumeCubicMeters?: number;
  vehicleCondition: string;
  insuranceProvider?: string;
  insurancePolicyNumber?: string;
  insuranceExpiryDate?: string;
  registrationExpiryDate?: string;
  imageUrl?: string;
};

export function myVehicles() {
  return apiRequest("GET", "/api/vehicles/my");
}

export function createVehicle(payload: VehicleRequest) {
  return apiRequest("POST", "/api/vehicles", payload);
}

export function updateVehicle(id: number, payload: VehicleRequest) {
  return apiRequest("PUT", `/api/vehicles/${id}`, payload);
}

export function deleteVehicle(id: number) {
  return apiRequest("DELETE", `/api/vehicles/${id}`);
}

export function findVehicle(id: number) {
  return apiRequest("GET", `/api/vehicles/${id}`);
}

