<template>

    <ScrollView>
        <StackLayout class="p-4">

            <Label text="Delivery info" class="text-lg font-bold mb-3" />

            <TextField v-model="title" hint="Title" class="input mb-2" />
            <TextField v-model="description" hint="Description" class="input mb-2" />

            <TextField v-model="dropoffAddress" hint="Dropoff address" class="input mb-2" />
            <TextField v-model="dropoffLat" hint="Dropoff latitude" keyboardType="number" class="input mb-2" />
            <TextField v-model="dropoffLng" hint="Dropoff longitude" keyboardType="number" class="input mb-2" />

            <TextField v-model="dropoffContactName" hint="Dropoff contact name" class="input mb-2" />

            <TextField v-model="dropoffContactPhone" hint="Dropoff contact phone" keyboardType="phone"
                class="input mb-4" />

            <Label text="Delivery type" class="font-bold mb-1" />

            <ListPicker :items="deliveryTypeLabels" :selectedIndex="deliveryTypeIndex"
                @selectedIndexChange="onDeliveryTypeChange" class="mb-4" />

            <TextField v-show="selectedDeliveryType !== 'PASSENGER'" v-model="packageWeight" hint="Weight (kg)"
                keyboardType="number" class="input mb-4" />
            <TextField v-model="pickupContactName" hint="Pickup contact name" class="input mb-2" />

            <TextField v-model="pickupContactPhone" hint="Pickup contact phone" keyboardType="phone"
                class="input mb-4" />
            <Button text="Estimate fare" class="btn-outline mb-3" @tap="estimate" />

            <StackLayout v-if="fare">
                <Label :text="`Estimated price: ${fare.totalFare} ден`" class="font-bold mb-1" />
                <Label :text="`Distance: ${fare.distanceKm} km`" />
                <Label :text="`ETA: ${fare.estimatedMinutes} min`" class="mb-3" />
            </StackLayout>

            <Button text="Create delivery" class="btn-primary" :isEnabled="!!fare && !loading" @tap="create" />

        </StackLayout>
    </ScrollView>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { getCurrentLocation, enableLocationRequest } from "@nativescript/geolocation";
import * as DeliveryService from "../../services/deliveries.service";
import { authStore } from "~/stores/auth.store";
import { alert } from "@nativescript/core";
import type { DeliveryType } from "../../services/deliveries.service";
//User
const auth = computed(() => authStore.user);
//Form fields
const title = ref("");
const description = ref("");

const dropoffAddress = ref("");
const dropoffLat = ref("");
const dropoffLng = ref("");
const dropoffContactName = ref("");
const dropoffContactPhone = ref("");


const pickupContactName = ref("");
const pickupContactPhone = ref("");

const packageWeight = ref("");


const pickupLat = ref<number | null>(null);
const pickupLng = ref<number | null>(null);


const fare = ref<any>(null);
const loading = ref(false);

//Delivery types
const deliveryTypes: { value: DeliveryType; label: string }[] = [
    { value: "PACKAGE", label: "Package delivery" },
    { value: "DOCUMENTS", label: "Documents" },
    { value: "PASSENGER", label: "Passenger transport" },
];

const deliveryTypeIndex = ref(0);

const deliveryTypeLabels = deliveryTypes.map(t => t.label);

const selectedDeliveryType = computed<DeliveryType>(
    () => deliveryTypes[deliveryTypeIndex.value].value
)

function onDeliveryTypeChange(e: any) {
    deliveryTypeIndex.value = e.value;
}



onMounted(async () => {
    await enableLocationRequest(true);
    const loc = await getCurrentLocation({ timeout: 20000 });
    pickupLat.value = loc.latitude;
    pickupLng.value = loc.longitude;

    if (auth.value) {
        pickupContactName.value = `${auth.value.firstName} ${auth.value.lastName}`;
        pickupContactPhone.value = auth.value.phoneNumber;
    }
});

function validate(): boolean {
    if (
        !dropoffAddress.value ||
        !dropoffContactName.value ||
        !dropoffContactPhone.value ||
        !pickupContactPhone.value
    ) {
        alert("Please fill all contact details.");
        return false;
    }

    if (isNaN(Number(dropoffLat.value)) || isNaN(Number(dropoffLng.value))) {
        alert("Invalid dropoff coordinates.");
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
    if (pickupLat.value == null || pickupLng.value == null) return;
    if (!validate()) return;

    fare.value = await DeliveryService.estimateFare({
        pickupLatitude: pickupLat.value,
        pickupLongitude: pickupLng.value,
        dropoffLatitude: Number(dropoffLat.value),
        dropoffLongitude: Number(dropoffLng.value),
        packageType: selectedDeliveryType.value,
        packageWeight:
            selectedDeliveryType.value === "PASSENGER"
                ? 0
                : Number(packageWeight.value),
    });
}

async function create() {
    if (pickupLat.value == null || pickupLng.value == null) return;
    if (!validate()) return;

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

            dropoffAddress: dropoffAddress.value,
            dropoffLatitude: Number(dropoffLat.value),
            dropoffLongitude: Number(dropoffLng.value),
            dropoffContactName: dropoffContactName.value,
            dropoffContactPhone: dropoffContactPhone.value,

            requestedPickupTime: new Date().toISOString(),
            city: "Skopje",

        });

        // reset everything
        fare.value = null;
        title.value = description.value = "";
        dropoffAddress.value = dropoffLat.value = dropoffLng.value = "";
        dropoffContactName.value = dropoffContactPhone.value = "";
        packageWeight.value = "";
    } finally {
        loading.value = false;
    }
}
</script>