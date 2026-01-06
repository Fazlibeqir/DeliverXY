<template>
  <Page>
    <ActionBar title="Top up wallet" />

    <ScrollView>
      <StackLayout class="p-4">

        <!-- Amount -->
        <Label text="Amount" class="text-gray-600 mb-1" />
        <TextField v-model="amount" keyboardType="number" hint="Enter amount" class="input mb-4" />

        <!-- Card Number -->
        <Label text="Card number" class="text-gray-600 mb-1" />
        <TextField v-model="cardNumber" hint="4242 4242 4242 4242" keyboardType="number" class="input mb-1"
          @textChange="onCardNumberInput" />
        <Label v-if="cardError" :text="cardError" class="text-red-500 text-xs mb-3" />

        <!-- Expiry + CVV -->
        <GridLayout columns="*,*" class="mb-3">

          <!-- Expiry -->
          <StackLayout col="0" class="mr-2">
            <Label text="Expiry (MM/YY)" class="text-gray-600 mb-1" />
            <TextField :text="formattedExpiry" 
            hint="MM/YY" 
            keyboardType="number" 
            class="input mb-1"
            autocorrect="false"
            autocapitalizationType="none"
              @textChange="onExpiryInput" />
            <Label v-if="expiryError" :text="expiryError" class="text-red-500 text-xs" />
          </StackLayout>

          <!-- CVV -->
          <StackLayout col="1" class="ml-2">
            <Label text="CVV" class="text-gray-600 mb-1" />
            <TextField v-model="cvv" hint="123" keyboardType="number" secure="true" class="input mb-1"
              @textChange="onCvvInput" />
            <Label v-if="cvvError" :text="cvvError" class="text-red-500 text-xs" />
          </StackLayout>

        </GridLayout>

        <!-- Pay -->
        <Button text="Pay (Mock)" class="btn-primary" :isEnabled="formValid && !loading" @tap="topUp" />

        <ActivityIndicator v-if="loading" busy="true" class="mt-4" />

      </StackLayout>
    </ScrollView>
  </Page>
</template>




<script setup lang="ts">
import { ref, computed } from "vue";
import { alert, Frame } from "@nativescript/core";
import { initiateTopUp } from "@/services/wallet.service";

const amount = ref("");
const cardNumber = ref("");
const expiryDigits  = ref("");
const cvv = ref("");
const loading = ref(false);

/* ---------------- CARD NUMBER ---------------- */
function onCardNumberInput(e: any) {
  let digits = e.value.replace(/\D/g, "").slice(0, 16);
  cardNumber.value = digits.replace(/(.{4})/g, "$1 ").trim();
}

const cardError = computed(() => {
  const clean = cardNumber.value.replace(/\s/g, "");
  if (!clean) return null;
  if (clean.length < 16) return "Card number must be 16 digits";
  return null;
});

/* ---------------- EXPIRY ---------------- */
function onExpiryInput(e: any) {
  let digits = e.value.replace(/\D/g, "");

  // Force first digit to be month-related
  if (digits.length > 4) {
    digits = digits.slice(0, 4);
  }

  if (digits.length === 1 && Number(digits) > 1) {
    digits = "0" + digits;
  }

  // Validate month once we have MM
  if (digits.length >= 2) {
    let mm = Number(digits.slice(0, 2));
    if (mm === 0) mm = 1;
    if (mm > 12) mm = 12;
    digits = mm.toString().padStart(2, "0") + digits.slice(2);
  }

  expiryDigits.value = digits;
}


const formattedExpiry = computed(() => {
  if (expiryDigits.value.length <= 2) return expiryDigits.value;
  return expiryDigits.value.slice(0, 2) + "/" + expiryDigits.value.slice(2);
});




const expiryError = computed(() => {
  if (expiryDigits.value.length !== 4) return "Invalid expiry";

  const mm = Number(expiryDigits.value.slice(0, 2));
  const yy = Number(expiryDigits.value.slice(2));

  const now = new Date();
  const currentYear = now.getFullYear() % 100;
  const currentMonth = now.getMonth() + 1;

  if (yy < currentYear) return "Card expired";
  if (yy === currentYear && mm < currentMonth) return "Card expired";

  return null;
});


/* ---------------- CVV ---------------- */
function onCvvInput(e: any) {
  cvv.value = e.value.replace(/\D/g, "").slice(0, 3);
}

const cvvError = computed(() => {
  if (!cvv.value) return null;
  if (cvv.value.length !== 3) return "CVV must be 3 digits";
  return null;
});

/* ---------------- FORM VALID ---------------- */
const formValid = computed(() => 
  Number(amount.value) > 0 &&
  expiryDigits.value.length === 4 &&
  cvv.value.length === 3 &&
  cardNumber.value.replace(/\s/g, "").length === 16 &&
  !expiryError.value &&
  !cvvError.value &&
  !cardError.value
);

/* ---------------- SUBMIT (MOCK) ---------------- */
async function topUp() {
  console.log("Top up initiated");
  try {
    loading.value = true;
    await initiateTopUp(Number(amount.value), "MOCK");

    alert("Payment successful 💳💰");
    (globalThis as any).__refreshWallet?.();
    Frame.topmost().goBack();

  } catch (e: any) {
    alert(e.message || "Payment failed");
  } finally {
    loading.value = false;
  }
}
</script>