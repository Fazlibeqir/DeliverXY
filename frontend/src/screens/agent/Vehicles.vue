<template>
  <Page>
    <ActionBar title="My Vehicles" />

    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <Button text="➕ Add Vehicle" class="btn-primary mb-4" @tap="createNew" />

        <StackLayout v-for="v in vehicles" :key="v.id" class="card-elevated p-4 mb-3">
          <Image v-if="v.imageUrl && cachedImages[v.id]" :src="cachedImages[v.id]" height="120" stretch="aspectFill" 
            class="mb-3 rounded-lg" />
          <ActivityIndicator v-else-if="v.imageUrl && loadingImages[v.id]" busy="true" height="120" />
          <Label :text="`${v.make} ${v.model}`" class="font-bold text-lg mb-1" />
          <Label :text="`${v.vehicleYear || 'Year not specified'}`" class="text-secondary text-sm mb-2" />
          <StackLayout class="mb-3">
            <Label text="License Plate" class="text-xs text-secondary mb-1" />
            <Label :text="v.licensePlate" class="font-semibold" />
          </StackLayout>
          <StackLayout class="divider" />
          <GridLayout columns="*,*" class="mt-3">
            <Button col="0" text="Edit" class="btn-outline mr-1" @tap="edit(v.id)" />
            <Button col="1" text="Delete" class="btn-danger ml-1" @tap="remove(v.id)" />
          </GridLayout>
        </StackLayout>

        <StackLayout v-if="!loading && vehicles.length === 0" class="empty-state">
          <Label text="🚗" class="empty-state-icon" />
          <Label text="No vehicles yet" class="empty-state-text" />
          <Label text="Add your first vehicle to start delivering" class="text-secondary text-sm mt-2" />
        </StackLayout>

        <StackLayout v-if="loading" class="loading-container">
          <ActivityIndicator busy="true" />
          <Label text="Loading vehicles..." class="loading-text" />
        </StackLayout>
      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { alert, confirm } from "@nativescript/core";
import { $navigateTo } from "nativescript-vue";
import * as VehiclesService from "@/services/vehicles.service";
import VehicleForm from "./VehiclesForm.vue";
import { getAuthenticatedImageUrl } from "@/services/api";

const vehicles = ref<any[]>([]);
const loading = ref(false);
const cachedImages = ref<Record<number, string>>({});
const loadingImages = ref<Record<number, boolean>>({});

async function load() {
  loading.value = true;
  try {
    vehicles.value = await VehiclesService.myVehicles();
    const imagePromises = vehicles.value
      .filter(v => v.imageUrl)
      .map(async (vehicle) => {
        loadingImages.value[vehicle.id] = true;
        try {
          const cachedUrl = await getAuthenticatedImageUrl(vehicle.imageUrl);
          if (cachedUrl) {
            cachedImages.value[vehicle.id] = cachedUrl;
          }
        } catch (e) {
          console.error(`Failed to load image for vehicle ${vehicle.id}:`, e);
        } finally {
          loadingImages.value[vehicle.id] = false;
        }
      });
    
    await Promise.all(imagePromises);
  } catch {
    alert("Failed to load vehicles");
  } finally {
    loading.value = false;
  }
}

onMounted(load);

function createNew() {
  $navigateTo(VehicleForm, { props: { onSaved: load } });
}

function edit(id: number) {
  $navigateTo(VehicleForm, { props: { id, onSaved: load } });
}

async function remove(id: number) {
  const ok = await confirm("Delete this vehicle?");
  if (!ok) return;
  try {
    await VehiclesService.deleteVehicle(id);
    await load();
  } catch (e: any) {
    alert(e?.message || "Failed to delete vehicle");
  }
}
</script>