<template>
  <Page>
    <ActionBar title="Delivery Details">
      <NavigationButton text="← Back" @tap="goBack" />
    </ActionBar>

    <GridLayout rows="*, auto">
      <!-- Form Content -->
      <ScrollView ref="scrollView" row="0">
        <StackLayout class="p-4">
          <!-- Basic Information -->
          <StackLayout class="mb-4">
            <Label text="Basic Information" class="text-lg font-bold mb-3" />
            
            <StackLayout class="mb-3">
              <Label text="Title *" class="text-sm mb-1" />
              <TextField 
                v-model="title" 
                hint="e.g., Package to downtown" 
                class="input"
                :class="{ 'input-error': errors.title }"
                @textChange="validateTitle"
              />
              <Label v-if="errors.title" :text="errors.title" class="text-error text-xs mt-1" />
            </StackLayout>

            <StackLayout class="mb-3">
              <Label text="Description (Optional)" class="text-sm mb-1" />
              <TextView v-model="description" hint="Additional details" class="input" 
                        style="min-height: 100;" />
            </StackLayout>
          </StackLayout>

          <!-- Contact Information -->
          <StackLayout class="mb-4">
            <Label text="Contact Information" class="text-lg font-bold mb-3" />
            
            <Label text="Recipient (Dropoff)" class="text-xs text-secondary mb-2" />
            <StackLayout class="mb-3">
              <Label text="Name *" class="text-sm mb-1" />
              <TextField 
                v-model="dropoffContactName" 
                hint="Recipient name" 
                class="input"
                :class="{ 'input-error': errors.dropoffContactName }"
                @textChange="validateDropoffContactName"
              />
              <Label v-if="errors.dropoffContactName" :text="errors.dropoffContactName" class="text-error text-xs mt-1" />
            </StackLayout>

            <StackLayout class="mb-3">
              <Label text="Phone *" class="text-sm mb-1" />
              <TextField 
                v-model="dropoffContactPhone" 
                hint="Recipient phone" 
                keyboardType="phone" 
                class="input"
                :class="{ 'input-error': errors.dropoffContactPhone }"
                @textChange="validateDropoffContactPhone"
              />
              <Label v-if="errors.dropoffContactPhone" :text="errors.dropoffContactPhone" class="text-error text-xs mt-1" />
            </StackLayout>

            <Label text="You (Pickup)" class="text-xs text-secondary mb-2 mt-2" />
            <StackLayout class="mb-3">
              <Label text="Name *" class="text-sm mb-1" />
              <TextField 
                v-model="pickupContactName" 
                hint="Your name" 
                class="input"
                :class="{ 'input-error': errors.pickupContactName }"
                @textChange="validatePickupContactName"
              />
              <Label v-if="errors.pickupContactName" :text="errors.pickupContactName" class="text-error text-xs mt-1" />
            </StackLayout>

            <StackLayout class="mb-3">
              <Label text="Phone *" class="text-sm mb-1" />
              <TextField 
                v-model="pickupContactPhone" 
                hint="Your phone" 
                keyboardType="phone" 
                class="input"
                :class="{ 'input-error': errors.pickupContactPhone }"
                @textChange="validatePickupContactPhone"
              />
              <Label v-if="errors.pickupContactPhone" :text="errors.pickupContactPhone" class="text-error text-xs mt-1" />
            </StackLayout>
          </StackLayout>

          <!-- Delivery Details -->
          <StackLayout class="mb-4">
            <Label text="Delivery Details" class="text-lg font-bold mb-3" />
            
            <StackLayout class="mb-3">
              <Label text="Type *" class="text-sm mb-1" />
              <ListPicker :items="deliveryTypeLabels" :selectedIndex="deliveryTypeIndex" 
                          @selectedIndexChange="onDeliveryTypeChange" class="input" />
            </StackLayout>

            <StackLayout v-show="selectedDeliveryType !== 'PASSENGER'" class="mb-3">
              <Label text="Weight (kg) *" class="text-sm mb-1" />
              <TextField 
                v-model="packageWeight" 
                hint="Enter weight" 
                keyboardType="number" 
                class="input"
                :class="{ 'input-error': errors.packageWeight }"
                @textChange="validatePackageWeight"
              />
              <Label v-if="errors.packageWeight" :text="errors.packageWeight" class="text-error text-xs mt-1" />
            </StackLayout>

            <StackLayout class="mb-3">
              <Label text="Promo Code (Optional)" class="text-sm mb-1" />
              <TextField v-model="promoCode" hint="Enter promo code" 
                         autocapitalizationType="allcharacters" class="input" />
            </StackLayout>
          </StackLayout>

          <!-- Fare Display -->
          <StackLayout ref="fareDisplay" v-if="fare" class="mb-4 p-4 fare-display" style="background-color: #e8f5e9; border-radius: 8;">
            <Label text="💵 Fare Estimate" class="text-base font-bold mb-2" />
            <Label :text="`${Number(fare.totalFare).toFixed(2)} ден`" class="text-3xl font-bold text-primary mb-2" />
            <GridLayout columns="*, *" class="mt-2">
              <Label col="0" :text="`📏 ${Number(fare.distanceKm).toFixed(2)} km`" class="text-sm text-secondary" />
              <Label col="1" :text="`⏱️ ${fare.estimatedMinutes} min`" class="text-sm text-secondary" />
            </GridLayout>
            <Label v-if="fare.discount && Number(fare.discount) > 0" 
                   :text="`🎉 Discount: -${Number(fare.discount).toFixed(2)} ден`" 
                   class="text-sm text-success mt-2" />
          </StackLayout>
        </StackLayout>
      </ScrollView>

      <!-- Footer Actions -->
      <StackLayout row="1" class="p-4 bg-white" style="border-top-width: 1; border-top-color: #e0e0e0;">
        <GridLayout columns="*, *" columnsGap="8">
          <Button col="0" 
                  v-if="!fare"
                  :text="loading ? '⏳ Estimating...' : '💰 Estimate Fare'" 
                  class="btn-primary" 
                  :isEnabled="!loading && canEstimate"
                  @tap="estimate" />
          <Button col="0" 
                  v-if="fare"
                  :text="loading ? '⏳ Creating...' : '✓ Create Delivery'" 
                  class="btn-primary" 
                  :isEnabled="!loading"
                  @tap="create" />
          <Button col="1" text="Cancel" class="btn-outline" @tap="goBack" />
        </GridLayout>
      </StackLayout>
    </GridLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { alert, Frame, ScrollView } from "@nativescript/core";
import * as DeliveryService from "../../services/deliveries.service";
import { authStore } from "~/stores/auth.store";
import type { DeliveryType } from "../../services/deliveries.service";
import { $showModal } from "nativescript-vue";
import WalletPaymentModal from "@/components/payments/WalletPaymentModal.vue";

const props = defineProps<{
  pickupLat: number;
  pickupLng: number;
  pickupAddress: string;
  dropoffLat: number;
  dropoffLng: number;
  dropoffAddress: string;
}>();

const auth = computed(() => authStore.user);

const title = ref("");
const description = ref("");
const dropoffContactName = ref("");
const dropoffContactPhone = ref("");
const pickupContactName = ref("");
const pickupContactPhone = ref("");
const packageWeight = ref("");
const promoCode = ref("");
const loading = ref(false);
const fare = ref<any>(null);
const fareDisplay = ref<any>(null);
const scrollView = ref<any>(null);
const errors = ref<Record<string, string>>({});

const deliveryTypes: { value: DeliveryType; label: string }[] = [
  { value: "PACKAGE", label: "Package delivery" },
  { value: "DOCUMENTS", label: "Documents" },
  { value: "PASSENGER", label: "Passenger transport" },
];

const deliveryTypeIndex = ref(0);
const selectedDeliveryType = computed<DeliveryType>(() => deliveryTypes[deliveryTypeIndex.value].value);
const deliveryTypeLabels = deliveryTypes.map(t => t.label);

function onDeliveryTypeChange(e: any) {
  deliveryTypeIndex.value = e.value;
  // Clear weight error if switching to PASSENGER
  if (selectedDeliveryType.value === "PASSENGER") {
    delete errors.value.packageWeight;
  }
}

// Validation functions
function validateTitle() {
  if (!title.value || title.value.trim().length === 0) {
    errors.value.title = "Title is required";
  } else if (title.value.trim().length < 3) {
    errors.value.title = "Title must be at least 3 characters";
  } else {
    delete errors.value.title;
  }
}

function validateDropoffContactName() {
  if (!dropoffContactName.value || dropoffContactName.value.trim().length === 0) {
    errors.value.dropoffContactName = "Recipient name is required";
  } else {
    delete errors.value.dropoffContactName;
  }
}

function validateDropoffContactPhone() {
  if (!dropoffContactPhone.value || dropoffContactPhone.value.trim().length === 0) {
    errors.value.dropoffContactPhone = "Recipient phone is required";
  } else if (!/^[+]?[\d\s-]{8,}$/.test(dropoffContactPhone.value.trim())) {
    errors.value.dropoffContactPhone = "Please enter a valid phone number";
  } else {
    delete errors.value.dropoffContactPhone;
  }
}

function validatePickupContactName() {
  if (!pickupContactName.value || pickupContactName.value.trim().length === 0) {
    errors.value.pickupContactName = "Your name is required";
  } else {
    delete errors.value.pickupContactName;
  }
}

function validatePickupContactPhone() {
  if (!pickupContactPhone.value || pickupContactPhone.value.trim().length === 0) {
    errors.value.pickupContactPhone = "Your phone is required";
  } else if (!/^[+]?[\d\s-]{8,}$/.test(pickupContactPhone.value.trim())) {
    errors.value.pickupContactPhone = "Please enter a valid phone number";
  } else {
    delete errors.value.pickupContactPhone;
  }
}

function validatePackageWeight() {
  if (selectedDeliveryType.value === "PASSENGER") {
    delete errors.value.packageWeight;
    return;
  }
  const weight = Number(packageWeight.value);
  if (!packageWeight.value || packageWeight.value.trim().length === 0) {
    errors.value.packageWeight = "Weight is required";
  } else if (isNaN(weight) || weight <= 0) {
    errors.value.packageWeight = "Please enter a valid weight (greater than 0)";
  } else if (weight > 1000) {
    errors.value.packageWeight = "Weight cannot exceed 1000 kg";
  } else {
    delete errors.value.packageWeight;
  }
}

const canEstimate = computed(() => {
  return title.value && 
         dropoffContactName.value && 
         dropoffContactPhone.value && 
         pickupContactName.value && 
         pickupContactPhone.value &&
         (selectedDeliveryType.value === "PASSENGER" || Number(packageWeight.value) > 0);
});

onMounted(() => {
  if (auth.value) {
    pickupContactName.value = `${auth.value.firstName} ${auth.value.lastName}`;
    pickupContactPhone.value = auth.value.phoneNumber;
  }
});

function goBack() {
  Frame.topmost()?.goBack();
}

async function estimate() {
  // Validate all fields
  validateTitle();
  validateDropoffContactName();
  validateDropoffContactPhone();
  validatePickupContactName();
  validatePickupContactPhone();
  validatePackageWeight();
  
  // Check if there are any errors
  if (Object.keys(errors.value).length > 0) {
    alert("Please fix the errors in the form");
    return;
  }
  
  if (!canEstimate.value) {
    alert("Please fill all required fields");
    return;
  }

  loading.value = true;
  try {
    fare.value = await DeliveryService.estimateFare({
      pickupLatitude: props.pickupLat,
      pickupLongitude: props.pickupLng,
      dropoffLatitude: props.dropoffLat,
      dropoffLongitude: props.dropoffLng,
      packageType: selectedDeliveryType.value,
      packageWeight: selectedDeliveryType.value === "PASSENGER" ? 0 : Number(packageWeight.value),
      promoCode: promoCode.value?.trim() || undefined,
    });
    
    // Scroll to show the fare estimate results
    await nextTick();
    if (scrollView.value && scrollView.value.nativeView) {
      const scrollViewNative = scrollView.value.nativeView as ScrollView;
      // Wait for DOM to update, then scroll to bottom to show fare display
      setTimeout(() => {
        // Scroll to a large offset to ensure fare display is visible
        // Using scrollableHeight would be ideal, but scrollToVerticalOffset with a large value works
        const maxScroll = scrollViewNative.scrollableHeight || 2000;
        scrollViewNative.scrollToVerticalOffset(maxScroll, true);
      }, 150);
    }
  } catch (e: any) {
    alert(e?.message || "Failed to estimate fare");
  } finally {
    loading.value = false;
  }
}

async function create() {
  if (!fare.value) {
    alert("Please estimate fare first");
    return;
  }

  // Show payment modal
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
      packageWeight: selectedDeliveryType.value === "PASSENGER" ? 0 : Number(packageWeight.value),
      pickupAddress: props.pickupAddress || "Current location",
      pickupLatitude: props.pickupLat,
      pickupLongitude: props.pickupLng,
      pickupContactName: pickupContactName.value,
      pickupContactPhone: pickupContactPhone.value,
      dropoffAddress: props.dropoffAddress,
      dropoffLatitude: props.dropoffLat,
      dropoffLongitude: props.dropoffLng,
      dropoffContactName: dropoffContactName.value,
      dropoffContactPhone: dropoffContactPhone.value,
      requestedPickupTime: new Date().toISOString(),
      city: "Skopje",
      paymentProvider: "WALLET",
      promoCode: promoCode.value?.trim() || undefined,
    });

    alert("Delivery created successfully!");
    goBack();
  } catch (e: any) {
    alert(e?.message || "Failed to create delivery");
  } finally {
    loading.value = false;
  }
}
</script>
