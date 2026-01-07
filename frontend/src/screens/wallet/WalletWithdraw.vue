<template>
    <Page>
      <ActionBar title="Withdraw" />
  
      <StackLayout class="p-4">
        <Label text="Amount" class="mb-2 text-gray-600" />
  
        <TextField
          v-model="amount"
          keyboardType="number"
          hint="Enter amount"
          class="input mb-4"
        />
  
        <Button
          :isEnabled="!loading"
          text="Withdraw"
          class="btn-primary"
          @tap="withdraw"
        />
  
        <ActivityIndicator v-if="loading" busy="true" class="mt-4" />
      </StackLayout>
    </Page>
  </template>
  
  <script setup lang="ts">
  import { ref } from "vue";
  import { alert } from "@nativescript/core";
  import { withdrawFromWallet } from "@/services/wallet.service";
  import { Frame } from "@nativescript/core";


  const amount = ref("");
  const loading = ref(false);
  
  async function withdraw() {
    if (!amount.value || Number(amount.value) <= 0) {
      alert("Enter a valid amount");
      return;
    }
  
    try {
      loading.value = true;
      await withdrawFromWallet(Number(amount.value));
  
      alert("Withdrawal successful");
      
    } catch (e: any) {
      alert(e.message || "Withdrawal failed");
    } finally {
      loading.value = false;
    }
  }
  </script>
  