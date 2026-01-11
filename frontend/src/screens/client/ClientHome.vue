<template>
    <GridLayout rows="*">
        <ScrollView row="0">
            <StackLayout class="p-4">
            <!-- Header Section -->
            <StackLayout class="mb-6">
                <Label text="📦 Create Delivery" class="section-header mb-2" />
                <Label text="Fill in the details below to create a new delivery request" class="text-secondary text-sm" />
            </StackLayout>

            <!-- Step 1: Basic Information -->
            <StackLayout class="card-elevated mb-4 p-4">
                <Label text="1️⃣ Basic Information" class="section-subheader mb-3 font-bold" />
                
                <StackLayout class="mb-3">
                    <Label text="Title *" class="section-subheader mb-1" />
                    <TextField v-model="title" hint="e.g., Package to downtown" class="input" />
                    <Label v-if="!title && fare" text="⚠️ Title is required" class="text-danger text-xs mt-1" />
                </StackLayout>

                <StackLayout class="mb-3">
                    <Label text="Description (Optional)" class="section-subheader mb-1" />
                    <TextField v-model="description" hint="Additional details about your delivery" class="input" />
                </StackLayout>
            </StackLayout>

            <!-- Step 2: Delivery Address -->
            <StackLayout class="card-elevated mb-4 p-4">
                <Label text="2️⃣ Delivery Address *" class="section-subheader mb-3 font-bold" />
                
                <StackLayout class="mb-2">
                    <TextField v-model="dropoffQuery" hint="📍 Type address, place name, or landmark" class="input" @textChange="onAddressInput" />
                    <Label v-if="!addressSelected && fare" text="⚠️ Please select an address from the list" class="text-danger text-xs mt-1" />
                    <Label v-else-if="addressSelected" text="✓ Address selected" class="text-success text-xs mt-1" />
                </StackLayout>
                
                <Label text="💡 Tip: Type a place name, mall, cafe, street or landmark" class="text-xs text-secondary mt-1 mb-2" />

                <StackLayout v-if="addressResults.length" class="card mb-2" style="max-height: 200;">
                    <ScrollView>
                        <StackLayout>
                            <Label 
                                v-for="(a, index) in addressResults" 
                                :key="a.label" 
                                :text="a.label" 
                                class="p-3"
                                :class="index < addressResults.length - 1 ? 'border-b border-gray-200' : ''"
                                @tap="selectAddress(a)" 
                            />
                        </StackLayout>
                    </ScrollView>
                </StackLayout>
            </StackLayout>

            <!-- Step 3: Contact Information -->
            <StackLayout class="card-elevated mb-4 p-4">
                <Label text="3️⃣ Contact Information *" class="section-subheader mb-3 font-bold" />
                
                <StackLayout class="mb-3">
                    <Label text="Recipient Name *" class="section-subheader mb-1" />
                    <TextField v-model="dropoffContactName" hint="Enter recipient's name" class="input" />
                    <Label v-if="!dropoffContactName && fare" text="⚠️ Required" class="text-danger text-xs mt-1" />
                </StackLayout>

                <StackLayout class="mb-3">
                    <Label text="Recipient Phone *" class="section-subheader mb-1" />
                    <TextField v-model="dropoffContactPhone" hint="Enter recipient's phone number" keyboardType="phone"
                        class="input" />
                    <Label v-if="!dropoffContactPhone && fare" text="⚠️ Required" class="text-danger text-xs mt-1" />
                </StackLayout>

                <StackLayout class="divider mb-3" />
                
                <StackLayout class="mb-3">
                    <Label text="Your Name (Pickup) *" class="section-subheader mb-1" />
                    <TextField v-model="pickupContactName" hint="Your name" class="input" />
                    <Label v-if="!pickupContactName && fare" text="⚠️ Required" class="text-danger text-xs mt-1" />
                </StackLayout>

                <StackLayout class="mb-2">
                    <Label text="Your Phone (Pickup) *" class="section-subheader mb-1" />
                    <TextField v-model="pickupContactPhone" hint="Your phone number" keyboardType="phone"
                        class="input" />
                    <Label v-if="!pickupContactPhone && fare" text="⚠️ Required" class="text-danger text-xs mt-1" />
                </StackLayout>
            </StackLayout>

            <!-- Step 4: Delivery Details -->
            <StackLayout class="card-elevated mb-4 p-4">
                <Label text="4️⃣ Delivery Details" class="section-subheader mb-3 font-bold" />
                
                <StackLayout class="mb-3">
                    <Label text="Delivery Type *" class="section-subheader mb-2" />
                    <ListPicker :items="deliveryTypeLabels" :selectedIndex="deliveryTypeIndex"
                        @selectedIndexChange="onDeliveryTypeChange" class="mb-2" />
                </StackLayout>

                <StackLayout v-show="selectedDeliveryType !== 'PASSENGER'" class="mb-3">
                    <Label text="Package Weight (kg) *" class="section-subheader mb-1" />
                    <TextField v-model="packageWeight" hint="Enter weight in kilograms"
                        keyboardType="number" class="input" />
                    <Label v-if="(!packageWeight || Number(packageWeight) <= 0) && fare && selectedDeliveryType !== 'PASSENGER'" 
                        text="⚠️ Please enter a valid weight" class="text-danger text-xs mt-1" />
                </StackLayout>

                <StackLayout class="mb-2">
                    <Label text="Promo Code (Optional)" class="section-subheader mb-1" />
                    <TextField v-model="promoCode" hint="Enter promo code to save money" class="input" autocapitalizationType="allcharacters" />
                </StackLayout>
            </StackLayout>

            <!-- Estimate Fare Button -->
            <Button 
                :text="loading ? '⏳ Estimating...' : '💰 Estimate Fare'" 
                class="btn-primary mb-4" 
                :isEnabled="addressSelected && !loading"
                @tap="estimate" 
            />

            <!-- Fare Estimate Card -->
            <StackLayout v-if="fare" class="card-elevated p-5 mb-4" style="background-color: #F8F9FA;">
                <StackLayout class="mb-3">
                    <Label text="💵 Fare Estimate" class="section-subheader mb-3 font-bold" />
                    <StackLayout class="mb-3">
                        <Label :text="`${Number(fare.totalFare).toFixed(2)} ден`" class="text-4xl font-bold text-primary mb-1" />
                        <Label text="Total Price" class="text-xs text-secondary" />
                    </StackLayout>
                </StackLayout>
                <StackLayout class="divider mb-3" />
                <StackLayout class="mt-2">
                    <StackLayout class="mb-2">
                        <Label text="📏 Distance" class="text-xs text-secondary mb-1" />
                        <Label :text="`${Number(fare.distanceKm).toFixed(2)} km`" class="text-sm font-semibold" />
                    </StackLayout>
                    <StackLayout class="mb-2">
                        <Label text="⏱️ Estimated Time" class="text-xs text-secondary mb-1" />
                        <Label :text="`${fare.estimatedMinutes} minutes`" class="text-sm font-semibold" />
                    </StackLayout>
                    <StackLayout v-if="fare.discount && Number(fare.discount) > 0" class="mb-2">
                        <Label text="🎉 Discount Applied" class="text-xs text-secondary mb-1" />
                        <Label :text="`-${Number(fare.discount).toFixed(2)} ден`" 
                            class="text-success text-sm font-semibold" />
                    </StackLayout>
                    <StackLayout v-if="fare.promoCodeApplied" class="mb-2">
                        <Label text="🎟️ Promo Code" class="text-xs text-secondary mb-1" />
                        <Label :text="fare.promoCodeApplied" 
                            class="text-secondary text-sm font-semibold" />
                    </StackLayout>
                </StackLayout>
            </StackLayout>

            <!-- Action Buttons -->
            <StackLayout v-if="fare">
                <Button 
                    :text="loading ? '⏳ Creating...' : '✓ Create Delivery'" 
                    class="btn-primary mb-3" 
                    :isEnabled="!!fare && !loading" 
                    @tap="create" 
                />
                <Button text="🔄 Reset Form" class="btn-outline" @tap="resetForm" />
            </StackLayout>

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