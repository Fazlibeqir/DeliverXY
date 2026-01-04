<template>
    <Page>
        <ActionBar title="KYC Verification" />
        <ScrollView>
            <StackLayout class="p-4">

                <Label text="KYC Verification" class="text-xl font-bold mb-3" />

                <Label :text="`Status: ${store.status ?? 'NOT SUBMITTED'}`" class="mb-4" />

                <Button text="Upload ID Front" @tap="pick('ID_FRONT')" />
                <Button text="Upload ID Back" @tap="pick('ID_BACK')" />
                <Button text="Upload Selfie" @tap="pick('SELFIE')" />
                <Button text="Upload Proof of Address" @tap="pick('PROOF_OF_ADDRESS')" />

                <Button text="Submit KYC" class="btn-primary mt-4" :isEnabled="canSubmit" @tap="submit" />

                <Label v-if="store.status === 'REJECTED'" :text="`Rejected: ${store.rejectionReason}`"
                    class="text-red-500 mt-3" />

            </StackLayout>
        </ScrollView>
    </Page>
</template>

<script setup lang="ts">
import { onMounted, computed } from "vue";
import { alert, ImageSource, knownFolders, path as nsPath } from "@nativescript/core";
import { useKYCStore } from "../../stores/kyc.store";
import * as KYCService from "../../services/kyc.service";

// ---- CAMERA (safe runtime load) ----
let takePicture: (options?: any) => Promise<any>;
let requestPermissions: () => Promise<void>;

try {
    // @ts-ignore
    const cam = require("@nativescript/camera");
    takePicture = cam.takePicture;
    requestPermissions = cam.requestPermissions;
} catch {
    throw new Error("Camera plugin not installed");
}

const store = useKYCStore();


onMounted(async () => {
    store.load();
     // This primes Android permission state (may NOT show popup)
  try {
    await requestPermissions();
  } catch {
    // Do nothing — popup will still appear on first camera open
  }
});

/**
 * Converts NativeScript ImageAsset to REAL file path
 */
 async function normalizeImagePath(imageAsset: any): Promise<string> {
  const imageSource = await ImageSource.fromAsset(imageAsset);

  if (!imageSource) {
    throw new Error("Failed to convert ImageAsset to ImageSource");
  }

  const folder = knownFolders.temp();
  const fileName = `kyc_${Date.now()}.jpg`;
  const filePath = nsPath.join(folder.path, fileName);

  const saved = imageSource.saveToFile(filePath, "jpg");

  if (!saved) {
    throw new Error("Failed to save image to file");
  }

  return filePath;
}


// ---- PICK & UPLOAD ----
async function pick(
    type: "ID_FRONT" | "ID_BACK" | "SELFIE" | "PROOF_OF_ADDRESS"
) {
   

    try {
    const image = await takePicture({
      width: 1024,
      height: 1024,
      keepAspectRatio: true,
      saveToGallery: false,
    });

    const filePath = await normalizeImagePath(image);

    const url = await KYCService.uploadKYCFile(filePath, type);

    if (type === "ID_FRONT") store.idFrontUrl = url;
    if (type === "ID_BACK") store.idBackUrl = url;
    if (type === "SELFIE") store.selfieUrl = url;
    if (type === "PROOF_OF_ADDRESS") store.proofOfAddressUrl = url;

  } catch (e) {
    console.error(e);
    await alert(
      "Failed to capture or upload image."
    );
  }
}

// ---- SUBMIT ----
const canSubmit = computed(() =>
    !!store.idFrontUrl &&
    !!store.idBackUrl &&
    !!store.selfieUrl &&
    !!store.proofOfAddressUrl &&
    store.status !== "APPROVED"
);

async function submit() {
    await store.submit();
    await alert("KYC submitted for review");
}
</script>