<template>
  <Page>
    <ActionBar :title="id ? 'Edit Vehicle' : 'Add Vehicle'" />
    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">
        <Label text="Vehicle Type" class="text-gray-600 mb-1" />
        <TextField v-model="vehicleType" class="input mb-3" hint="e.g., CAR, VAN, BIKE" autocapitalizationType="allcharacters" />

        <Label text="Make" class="text-gray-600 mb-1" />
        <TextField v-model="make" class="input mb-3" />

        <Label text="Model" class="text-gray-600 mb-1" />
        <TextField v-model="model" class="input mb-3" />

        <Label text="Year" class="text-gray-600 mb-1" />
        <TextField v-model="vehicleYear" class="input mb-1" keyboardType="number" maxLength="4" @textChange="onYearChange" />
        <Label v-if="yearError" :text="yearError" class="text-red-500 text-xs mb-3" />

        <Label text="License Plate" class="text-gray-600 mb-1" />
        <TextField v-model="licensePlate" class="input mb-1" autocapitalizationType="allcharacters" @textChange="onPlateChange" />
        <Label v-if="plateError" :text="plateError" class="text-red-500 text-xs mb-3" />

        <Label text="Color" class="text-gray-600 mb-1" />
        <TextField v-model="color" class="input mb-3" />

        <Label text="Condition" class="text-gray-600 mb-1" />
        <TextField v-model="vehicleCondition" class="input mb-3" hint="e.g., GOOD, EXCELLENT" autocapitalizationType="allcharacters" />

        <Label text="Vehicle Photo (optional)" class="text-gray-600 mb-1" />
        <Image v-if="cachedImageUrl" :src="cachedImageUrl" height="120" stretch="aspectFill" class="mb-2 rounded-lg" />
        <ActivityIndicator v-else-if="imageUrl && loadingImage" busy="true" height="120" />
        <Button :isEnabled="!uploading" text="Upload vehicle photo" class="btn-outline mb-3" @tap="uploadImage" />

        <Button :isEnabled="!loading" :text="id ? 'Save' : 'Create'" class="btn-primary" @tap="save" />
        <ActivityIndicator v-if="loading" busy="true" class="mt-3" />
      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { alert, Frame } from "@nativescript/core";
import * as VehiclesService from "@/services/vehicles.service";
import * as KYCService from "@/services/kyc.service";
import { getAuthenticatedImageUrl } from "@/services/api";
import { logger } from "@/utils/logger";

const props = defineProps<{ id?: number, onSaved?: () => void | Promise<void> }>();

const vehicleType = ref("");
const make = ref("");
const model = ref("");
const vehicleYear = ref("");
const licensePlate = ref("");
const color = ref("");
const vehicleCondition = ref("");
const loading = ref(false);
const plateError = ref<string | null>(null);
const yearError = ref<string | null>(null);
const imageUrl = ref<string>("");
const cachedImageUrl = ref<string>("");
const loadingImage = ref(false);
const uploading = ref(false);

let takePicture!: (options?: any) => Promise<any>;
let requestPermissions!: () => Promise<void>;
try {
  const cam = require("@nativescript/camera");
  takePicture = cam.takePicture;
  requestPermissions = cam.requestPermissions;
} catch (e) {}

onMounted(async () => {
  if (!props.id) return;
  try {
    loading.value = true;
    const v = await VehiclesService.findVehicle(props.id);
    vehicleType.value = v.vehicleType || "";
    make.value = v.make || "";
    model.value = v.model || "";
    vehicleYear.value = v.vehicleYear ? String(v.vehicleYear) : "";
    licensePlate.value = v.licensePlate || "";
    color.value = v.color || "";
    vehicleCondition.value = v.vehicleCondition || "";
    imageUrl.value = v.imageUrl || "";

    if (imageUrl.value) {
      loadingImage.value = true;
      try {
        const cached = await getAuthenticatedImageUrl(imageUrl.value);
        if (cached) {
          cachedImageUrl.value = cached;
        }
      } catch (e) {
        logger.error("Failed to load vehicle image:", e);
      } finally {
        loadingImage.value = false;
      }
    }
  } catch {
    alert("Failed to load vehicle");
  } finally {
    loading.value = false;
  }
});

function onPlateChange(e: any) {
  licensePlate.value = (e.value || "").toUpperCase().replace(/\s+/g, " ").trim();
  const normalized = licensePlate.value.replace(/\s/g, "");
  const valid = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/.test(normalized);
  plateError.value = normalized.length === 0 ? null : (valid ? null : "License plate must be in the format XX 000 XX");
}

function onYearChange(e: any) {
  const value = e.value || "";
  const digitsOnly = value.replace(/\D/g, "").substring(0, 4);
  vehicleYear.value = digitsOnly;

  if (digitsOnly.length === 0) {
    yearError.value = null;
    } else if (digitsOnly.length < 4) {
    yearError.value = null;
  } else {
    const year = Number(digitsOnly);
    if (year < 1900 || year > 2099) {
      yearError.value = "Year must be between 1900 and 2099";
    } else {
      yearError.value = null;
    }
  }
}

async function uploadImage() {
  if (!takePicture) {
    await alert("Camera plugin not installed.");
    return;
  }
  try {
    uploading.value = true;
    await requestPermissions?.();
    const image = await takePicture({
      width: 1024,
      height: 1024,
      keepAspectRatio: true,
      saveToGallery: false,
    });
    const temp = await (await import("@nativescript/core")).ImageSource.fromAsset(image);
    const folder = (await import("@nativescript/core")).knownFolders.temp();
    const pathUtil = (await import("@nativescript/core")).path;
    const filePath = pathUtil.join(folder.path, `vehicle_${Date.now()}.jpg`);
    const ok = temp.saveToFile(filePath, "jpg");
    if (!ok) throw new Error("Failed to save image");

    const url = await KYCService.uploadKYCFile(filePath, "VEHICLE_IMAGE" as any);
    imageUrl.value = url || "";
    if (url) {
      try {
        const cached = await getAuthenticatedImageUrl(url);
        if (cached) {
          cachedImageUrl.value = cached;
        } else {
          logger.error("Failed to cache uploaded image, it won't be displayed");
          cachedImageUrl.value = "";
        }
      } catch (e) {
        logger.error("Error caching uploaded image:", e);
        cachedImageUrl.value = "";
      }
    }
  } catch (e: any) {
    await alert(e?.message || "Image upload failed");
  } finally {
    uploading.value = false;
  }
}

async function save() {
  try {
    loading.value = true;
    const normalized = licensePlate.value.replace(/\s/g, "");
    if (!/^[A-Z]{2}[0-9]{3}[A-Z]{2}$/.test(normalized)) {
      plateError.value = "License plate must be in the format XX 000 XX";
      loading.value = false;
      return;
    }

    if (vehicleYear.value) {
      const year = Number(vehicleYear.value);
      if (isNaN(year) || year < 1900 || year > 2099) {
        yearError.value = "Year must be between 1900 and 2099";
        loading.value = false;
        return;
      }
    }
    
    const payload = {
      vehicleType: vehicleType.value.trim(),
      make: make.value.trim(),
      model: model.value.trim(),
      vehicleYear: vehicleYear.value ? Number(vehicleYear.value) : undefined,
      licensePlate: licensePlate.value.trim(),
      color: color.value.trim() || undefined,
      vehicleCondition: vehicleCondition.value.trim(),
      imageUrl: imageUrl.value || undefined,
    } as any;

    if (props.id) {
      await VehiclesService.updateVehicle(props.id, payload);
    } else {
      await VehiclesService.createVehicle(payload);
    }

    await props.onSaved?.();
    Frame.topmost().goBack();
  } catch (e: any) {
    alert(e?.message || "Failed to save vehicle");
  } finally {
    loading.value = false;
  }
}
</script>
