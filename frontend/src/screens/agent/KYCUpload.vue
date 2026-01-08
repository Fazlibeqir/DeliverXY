<template>
  <Page>
    <ActionBar title="KYC Verification" />
    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">

        <StackLayout class="card-elevated p-4 mb-4">
          <Label text="Verification Status" class="section-subheader mb-2" />
          <Label :text="getStatusLabel(store.status)" class="font-bold text-lg mb-2" :class="getStatusClass(store.status)" />
          <Label v-if="store.status === 'PENDING'" 
            text="Our team is reviewing your documents. This usually takes 24-48 hours."
            class="text-secondary text-sm" />
          <Label v-if="store.status === 'APPROVED'" 
            text="✓ Your account is verified and ready to accept deliveries."
            class="text-success text-sm" />
          <Label v-if="store.status === 'REJECTED'" 
            :text="`Reason: ${store.rejectionReason}`"
            class="text-danger text-sm mt-2" />
        </StackLayout>

        <StackLayout class="space-y-3">

          <KYCUploadItem label="ID Front" :done="!!store.idFrontUrl" :loading="uploading === 'ID_FRONT'"
            :disabled="store.status === 'PENDING' || store.status === 'APPROVED'" @tap="pick('ID_FRONT')" />

          <KYCUploadItem label="ID Back" :done="!!store.idBackUrl" :loading="uploading === 'ID_BACK'"
            :disabled="store.status === 'PENDING' || store.status === 'APPROVED'" @tap="pick('ID_BACK')" />

          <KYCUploadItem label="Selfie" :done="!!store.selfieUrl" :loading="uploading === 'SELFIE'"
            :disabled="store.status === 'PENDING' || store.status === 'APPROVED'" @tap="pick('SELFIE')" />

          <KYCUploadItem label="Proof of Address" :done="!!store.proofOfAddressUrl"
            :loading="uploading === 'PROOF_OF_ADDRESS'"
            :disabled="store.status === 'PENDING' || store.status === 'APPROVED'" @tap="pick('PROOF_OF_ADDRESS')" />

        </StackLayout>

        <Button v-if="store.status !== 'APPROVED'" text="Submit for Review" class="btn-primary-compact mt-4"
          :isEnabled="canSubmit && !uploading" :opacity="canSubmit ? 1 : 0.5" @tap="submit" />

      </StackLayout>
      </ScrollView>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useKYCStore } from "../../stores/kyc.store";
import * as KYCService from "../../services/kyc.service";
import KYCUploadItem from "./KYCUploadItem.vue";
import { alert,
  ImageSource,
  knownFolders,
  path as nsPath,
  Application,
  isAndroid,
  Frame} from "@nativescript/core";

const uploading = ref<null | "ID_FRONT" | "ID_BACK" | "SELFIE" | "PROOF_OF_ADDRESS">(null);
const props = defineProps<{ onDone?: () => Promise<void> | void }>();
const store = useKYCStore();
const disabledUploads = computed(() => store.status === "PENDING" || store.status === "APPROVED");

let takePicture!: (options?: any) => Promise<any>;
let requestPermissions!: () => Promise<void>;
try {
  const cam = require("@nativescript/camera");
  takePicture = cam.takePicture;
  requestPermissions = cam.requestPermissions;
} catch (e) {
  throw new Error("@nativescript/camera is not installed");
}

  function openAppSettings() {
  if (!isAndroid) return;

  const intent = new android.content.Intent(
    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
  );
  intent.setData(
    android.net.Uri.fromParts(
      "package",
      Application.android.context.getPackageName(),
      null as any
    )
  );
  intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
  Application.android.context.startActivity(intent);
}

async function ensureCameraPermission(): Promise<boolean> {
  try {
    await requestPermissions();
    return true;
  } catch {
    await alert({
      title: "Camera permission required",
      message:
        "Camera access is required to upload KYC documents.\n\n" +
        "Please enable it in App Settings.",
      okButtonText: "Open Settings",
    });

    openAppSettings();
    return false;
  }
}

onMounted(async () => {
  await store.load();
});

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

async function pick(
  type: "ID_FRONT" | "ID_BACK" | "SELFIE" | "PROOF_OF_ADDRESS"
) {

  if (uploading.value || disabledUploads.value) return;

  const ok = await ensureCameraPermission();
  if (!ok) return;

  uploading.value = type;

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
    await alert(
      "Failed to capture or upload image."
    );
  } finally {
    uploading.value = null;
  }
}

const canSubmit = computed(() =>
  !!store.idFrontUrl &&
  !!store.idBackUrl &&
  !!store.selfieUrl &&
  !!store.proofOfAddressUrl &&
  store.status !== "APPROVED" &&
  store.status !== "PENDING"
);

function getStatusLabel(status: string | null): string {
  if (!status) return "Not Submitted";
  switch (status) {
    case "APPROVED": return "✓ Approved";
    case "PENDING": return "⏳ Under Review";
    case "REJECTED": return "❌ Rejected";
    default: return "Not Submitted";
  }
}

function getStatusClass(status: string | null): string {
  if (!status) return "text-secondary";
  switch (status) {
    case "APPROVED": return "text-success";
    case "PENDING": return "text-warning";
    case "REJECTED": return "text-danger";
    default: return "text-secondary";
  }
}

async function submit() {
  await store.submit();
  await alert("KYC submitted for review");
  await props.onDone?.();
  Frame.topmost().goBack();
}
</script>