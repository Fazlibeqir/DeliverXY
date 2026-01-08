<template>
  <Page>
    <ActionBar title="Top up wallet" />

    <GridLayout rows="*">
      <ScrollView row="0">
        <StackLayout class="p-4">

        <Label text="Top Up Wallet" class="section-header mb-4" />

        <StackLayout class="mb-4">
          <Label text="Amount" class="section-subheader mb-1" />
          <TextField v-model="amount" keyboardType="number" hint="Enter amount in ден" class="input" />
        </StackLayout>

        <StackLayout class="mb-3">
          <Label text="Card Number" class="section-subheader mb-1" />
          <TextField v-model="cardNumber" hint="4242 4242 4242 4242" keyboardType="number" class="input"
            @textChange="onCardNumberInput" />
          <Label v-if="cardError" :text="cardError" class="text-danger text-xs mt-1" />
        </StackLayout>

        <GridLayout columns="*,*" class="mb-3">
          <StackLayout col="0" class="mr-2">
            <Label text="Expiry (MM/YY)" class="section-subheader mb-1" />
            <TextField :text="formattedExpiry" 
            hint="MM/YY" 
            keyboardType="number" 
            class="input"
            autocorrect="false"
            autocapitalizationType="none"
              @textChange="onExpiryInput" />
            <Label v-if="expiryError" :text="expiryError" class="text-danger text-xs mt-1" />
          </StackLayout>

          <StackLayout col="1" class="ml-2">
            <Label text="CVV" class="section-subheader mb-1" />
            <TextField v-model="cvv" hint="123" keyboardType="number" secure="true" class="input"
              @textChange="onCvvInput" />
            <Label v-if="cvvError" :text="cvvError" class="text-danger text-xs mt-1" />
          </StackLayout>

        </GridLayout>

        <Button text="💳 Pay Now" class="btn-primary mb-2" :isEnabled="formValid && !loading" @tap="topUp" />

        <StackLayout v-if="loading" class="loading-container">
          <ActivityIndicator busy="true" />
          <Label text="Processing payment..." class="loading-text" />
        </StackLayout>

      </StackLayout>
      </ScrollView>
    </GridLayout>
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

function onExpiryInput(e: any) {
  let digits = e.value.replace(/\D/g, "");

  if (digits.length > 4) {
    digits = digits.slice(0, 4);
  }

  if (digits.length === 1 && Number(digits) > 1) {
    digits = "0" + digits;
  }

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

function onCvvInput(e: any) {
  cvv.value = e.value.replace(/\D/g, "").slice(0, 3);
}

const cvvError = computed(() => {
  if (!cvv.value) return null;
  if (cvv.value.length !== 3) return "CVV must be 3 digits";
  return null;
});

const formValid = computed(() => 
  Number(amount.value) > 0 &&
  expiryDigits.value.length === 4 &&
  cvv.value.length === 3 &&
  cardNumber.value.replace(/\s/g, "").length === 16 &&
  !expiryError.value &&
  !cvvError.value &&
  !cardError.value
);

async function topUp() {
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