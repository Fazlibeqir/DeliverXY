<template>
    <GridLayout rows="*">
        <ScrollView row="0">
            <StackLayout class="p-4">
            <Label text="Create New Delivery" class="section-header mb-4" />

            <StackLayout class="mb-3">
                <Label text="Title" class="section-subheader mb-1" />
                <TextField v-model="title" hint="e.g., Package to downtown" class="input" />
            </StackLayout>

            <StackLayout class="mb-3">
                <Label text="Description" class="section-subheader mb-1" />
                <TextField v-model="description" hint="Additional details (optional)" class="input" />
            </StackLayout>

            <StackLayout class="mb-3">
                <Label text="Delivery Address" class="section-subheader mb-1" />
                <TextField v-model="dropoffQuery" hint="Type address, place name, or landmark" class="input" @textChange="onAddressInput" />
                <Label text="💡 Tip: Type a place name, mall, cafe, street or landmark" class="text-xs text-secondary mt-1" />
            </StackLayout>

            <StackLayout v-if="addressResults.length" class="card-elevated mb-3">
                <Label v-for="a in addressResults" :key="a.label" :text="a.label" class="p-3 border-b border-gray-200"
                    @tap="selectAddress(a)" />
            </StackLayout>

            <StackLayout class="mb-3">
                <Label text="Recipient Name" class="section-subheader mb-1" />
                <TextField v-model="dropoffContactName" hint="Enter recipient's name" class="input" />
            </StackLayout>

            <StackLayout class="mb-4">
                <Label text="Recipient Phone" class="section-subheader mb-1" />
                <TextField v-model="dropoffContactPhone" hint="Enter recipient's phone number" keyboardType="phone"
                    class="input" />
            </StackLayout>

            <StackLayout class="mb-4">
                <Label text="Delivery Type" class="section-subheader mb-2" />
                <ListPicker :items="deliveryTypeLabels" :selectedIndex="deliveryTypeIndex"
                    @selectedIndexChange="onDeliveryTypeChange" class="mb-2" />
            </StackLayout>

            <StackLayout v-show="selectedDeliveryType !== 'PASSENGER'" class="mb-4">
                <Label text="Package Weight (kg)" class="section-subheader mb-1" />
                <TextField v-model="packageWeight" hint="Enter weight in kilograms"
                    keyboardType="number" class="input" />
            </StackLayout>

            <StackLayout class="mb-3">
                <Label text="Your Name (Pickup)" class="section-subheader mb-1" />
                <TextField v-model="pickupContactName" hint="Your name" class="input" />
            </StackLayout>

            <StackLayout class="mb-4">
                <Label text="Your Phone (Pickup)" class="section-subheader mb-1" />
                <TextField v-model="pickupContactPhone" hint="Your phone number" keyboardType="phone"
                    class="input" />
            </StackLayout>

            <StackLayout class="mb-4">
                <Label text="Promo Code (Optional)" class="section-subheader mb-1" />
                <TextField v-model="promoCode" hint="Enter promo code" class="input" autocapitalizationType="allcharacters" />
            </StackLayout>
            <Button text="💰 Estimate Fare" class="btn-outline mb-4" :isEnabled="addressSelected && !loading"
                @tap="estimate" />

            <StackLayout v-if="fare" class="card-elevated p-4 mb-4">
                <Label text="Fare Estimate" class="section-subheader mb-3" />
                <StackLayout class="mb-2">
                    <Label :text="`${Number(fare.totalFare).toFixed(2)} ден`" class="text-3xl font-bold text-primary mb-1" />
                    <Label text="Total Price" class="text-xs text-secondary" />
                </StackLayout>
                <StackLayout class="divider" />
                <StackLayout class="mt-2">
                    <Label :text="`Distance: ${Number(fare.distanceKm).toFixed(2)} km`" class="text-sm mb-1" />
                    <Label :text="`Estimated Time: ${fare.estimatedMinutes} minutes`" class="text-sm mb-2" />
                    <Label v-if="fare.discount && Number(fare.discount) > 0" 
                        :text="`Discount: -${Number(fare.discount).toFixed(2)} ден`" 
                        class="text-success text-sm mb-1" />
                    <Label v-if="fare.promoCodeApplied" 
                        :text="`Promo: ${fare.promoCodeApplied}`" 
                        class="text-secondary text-xs" />
                </StackLayout>
            </StackLayout>

            <Button text="✓ Create Delivery" class="btn-primary mb-2" :isEnabled="!!fare && !loading" @tap="create" />
            <Button text="Reset Form" class="btn-secondary" @tap="resetForm" />

        </StackLayout>
        </ScrollView>
    </GridLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { getCurrentLocation, enableLocationRequest, isEnabled } from "@nativescript/geolocation";
import * as DeliveryService from "../../services/deliveries.service";
import { authStore } from "~/stores/auth.store";
import { alert, Application, Utils, isAndroid, isIOS } from "@nativescript/core";
import type { DeliveryType } from "../../services/deliveries.service";
import { searchAddress } from "@/services/geocoding.service";
import { $showModal } from "nativescript-vue";
import WalletPaymentModal from "@/components/payments/WalletPaymentModal.vue";

const auth = computed(() => authStore.user);

const title = ref("");
const description = ref("");

const dropoffQuery = ref("");
type AddressResult = { label: string; lat: number; lng: number };
const addressResults = ref<AddressResult[]>([]);
const addressSelected = ref<boolean>(false);
const suppressAddressInput = ref(false);
const selectedDropoffLat = ref<number | null>(null);
const selectedDropoffLng = ref<number | null>(null);
const dropoffContactName = ref("");
const dropoffContactPhone = ref("");


const pickupContactName = ref("");
const pickupContactPhone = ref("");

const packageWeight = ref("");


const pickupLat = ref<number | null>(null);
const pickupLng = ref<number | null>(null);


const fare = ref<any>(null);
const loading = ref(false);
const promoCode = ref("");

const deliveryTypes: { value: DeliveryType; label: string }[] = [
    { value: "PACKAGE", label: "Package delivery" },
    { value: "DOCUMENTS", label: "Documents" },
    { value: "PASSENGER", label: "Passenger transport" },
];

const deliveryTypeIndex = ref(0);
let addressTimeout: any = null;
function resetForm() {
    fare.value = null;
    title.value = description.value = "";
    dropoffQuery.value = "";
    addressResults.value = [];
    selectedDropoffLat.value = selectedDropoffLng.value = null;
    dropoffContactName.value = dropoffContactPhone.value = "";
    packageWeight.value = "";
}

async function onAddressInput(e: any) {
    if (suppressAddressInput.value) return;

    dropoffQuery.value = e.value;

    addressSelected.value = false;

    selectedDropoffLat.value = null;
    selectedDropoffLng.value = null;

    if (addressTimeout) clearTimeout(addressTimeout);

    if (dropoffQuery.value.length < 3) {
        addressResults.value = [];
        return;
    }

    addressTimeout = setTimeout(async () => {
        addressResults.value = await searchAddress(dropoffQuery.value);
    }, 400);
}

function selectAddress(a: AddressResult) {
    if (addressTimeout) clearTimeout(addressTimeout);

    suppressAddressInput.value = true;

    dropoffQuery.value = a.label;
    selectedDropoffLat.value = a.lat;
    selectedDropoffLng.value = a.lng;

    addressSelected.value = true;
    addressResults.value = [];
    setTimeout(() => {
        suppressAddressInput.value = false;
    }, 0);
}
const deliveryTypeLabels = deliveryTypes.map(t => t.label);

const selectedDeliveryType = computed<DeliveryType>(
    () => deliveryTypes[deliveryTypeIndex.value].value
)

function onDeliveryTypeChange(e: any) {
    deliveryTypeIndex.value = e.value;
}
function openAppSettings() {
    if (isAndroid) {
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
    } else if (isIOS) {
        Utils.openUrl("app-settings:");
    }
}

async function ensureLocationPermission(): Promise<boolean> {
    const enabled = await isEnabled();

    if (enabled) return true;

    try {
        await enableLocationRequest(true, true);
        return true;
    } catch {
        await alert({
            title: "Location Required",
            message:
                "Location access is required to use this feature.\n\n" +
                "Please enable it manually in App Settings.",
            okButtonText: "Open Settings",
        });

        openAppSettings();
        return false;
    }
}




onMounted(async () => {
    const ok = await ensureLocationPermission();
    if (!ok) return;
    const loc = await getCurrentLocation({ timeout: 20000 });
    pickupLat.value = loc.latitude;
    pickupLng.value = loc.longitude;

    if (auth.value) {
        pickupContactName.value = `${auth.value.firstName} ${auth.value.lastName}`;
        pickupContactPhone.value = auth.value.phoneNumber;
    }
});

function validate(): boolean {
    if (selectedDropoffLat.value == null || selectedDropoffLng.value == null) {
        alert("Please select an address from the list");
        return false;
    }
    if (
        !dropoffQuery.value ||
        !dropoffContactName.value ||
        !dropoffContactPhone.value ||
        !pickupContactPhone.value
    ) {
        alert("Please fill all contact details.");
        return false;
    }


    if (
        selectedDeliveryType.value !== "PASSENGER" &&
        Number(packageWeight.value) <= 0
    ) {
        alert("Please enter a valid package weight.");
        return false;
    }

    return true;
}

async function estimate() {
    if (addressResults.value.length > 0) {
        alert("Please select an address from the list");
        return;
    }

    if (
        pickupLat.value == null ||
        pickupLng.value == null ||
        selectedDropoffLat.value == null ||
        selectedDropoffLng.value == null
    ) {
        alert("Please select a dropoff address");
        return;
    }
    if (!validate()) return;

    fare.value = await DeliveryService.estimateFare({
        pickupLatitude: pickupLat.value,
        pickupLongitude: pickupLng.value,
        dropoffLatitude: selectedDropoffLat.value,
        dropoffLongitude: selectedDropoffLng.value,
        packageType: selectedDeliveryType.value,
        packageWeight:
            selectedDeliveryType.value === "PASSENGER"
                ? 0
                : Number(packageWeight.value),
        promoCode: promoCode.value?.trim() || undefined,
    });
}

async function create() {
    if (!fare.value) {
        alert("Please estimate fare first");
        return;
    }

    if (pickupLat.value == null || pickupLng.value == null) return;
    if (!validate()) return;

    const confirmed = await $showModal(WalletPaymentModal, {
        props: {
            amount: Number(fare.value.totalFare),
            distanceKm: Number(fare.value.distanceKm),
            etaMinutes: fare.value.estimatedMinutes,
        },
        fullscreen: false,
        animated: true,
    });

    if (!confirmed) return;

    loading.value = true;
    try {
        await DeliveryService.createDelivery({
            title: title.value,
            description: description.value,

            packageType: selectedDeliveryType.value,
            packageWeight: selectedDeliveryType.value === "PASSENGER"
                ? 0
                : Number(packageWeight.value),

            pickupAddress: "Current location",
            pickupLatitude: pickupLat.value,
            pickupLongitude: pickupLng.value,
            pickupContactName: pickupContactName.value,
            pickupContactPhone: pickupContactPhone.value,

            dropoffAddress: dropoffQuery.value,
            dropoffLatitude: selectedDropoffLat.value!,
            dropoffLongitude: selectedDropoffLng.value!,
            dropoffContactName: dropoffContactName.value,
            dropoffContactPhone: dropoffContactPhone.value,

            requestedPickupTime: new Date().toISOString(),
            city: "Skopje",
            paymentProvider: "WALLET",
            promoCode: promoCode.value?.trim() || undefined,

        });

        fare.value = null;
        title.value = description.value = "";
        dropoffQuery.value = "";
        addressResults.value = [];
        selectedDropoffLat.value = selectedDropoffLng.value = null;
        dropoffContactName.value = dropoffContactPhone.value = "";
        packageWeight.value = "";
    } finally {
        loading.value = false;
    }
}
</script>