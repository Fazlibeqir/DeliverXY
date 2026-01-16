<template>
  <Page>
    <ActionBar title="DeliverXY" />

    <TabView androidTabsPosition="bottom" @selectedIndexChanged="onTabChanged" v-model:selectedIndex="tabIndex">

      <TabViewItem title="Home" iconSource="res://ic_home">
        <!-- Keep HomeScreen always mounted to prevent map destruction -->
        <GridLayout>
          <HomeScreen />
        </GridLayout>
      </TabViewItem>

      <TabViewItem title="Deliveries" iconSource="res://ic_box">
        <GridLayout rows="*" columns="*">
          <!-- Loading State -->
          <StackLayout row="0" col="0" v-show="kycStore.loading" class="empty-state">
            <ActivityIndicator busy="true" class="mb-4" />
            <Label text="Checking KYC status..." class="text-secondary" />
          </StackLayout>
          
          <!-- KYC Approved - Show Deliveries -->
          <MyDeliveries 
            row="0" 
            col="0" 
            v-show="!kycStore.loading && kycStore.status === 'APPROVED'" 
          />
          
          <!-- KYC Not Approved or Not Submitted -->
          <StackLayout 
            row="0" 
            col="0" 
            v-show="!kycStore.loading && kycStore.status !== 'APPROVED'" 
            class="empty-state"
            style="padding: 40 20;"
          >
            <Label text="🔒" class="empty-state-icon" style="font-size: 64; margin-bottom: 16;" />
            <Label 
              :text="kycStore.status === 'REJECTED' ? 'KYC Verification Rejected' : 'KYC Verification Required'" 
              class="text-xl font-bold mb-2" 
            />
            <Label 
              :text="kycStore.status === 'REJECTED' 
                ? (kycStore.rejectionReason || 'Your KYC verification was rejected. Please resubmit with correct documents.')
                : 'You must complete KYC verification to accept deliveries.'" 
              class="empty-state-text mb-4" 
              textWrap="true"
            />
            <Button 
              :text="kycStore.status === 'REJECTED' ? 'Resubmit KYC' : 'Complete KYC'" 
              class="btn-primary" 
              @tap="goToKYC" 
            />
          </StackLayout>
        </GridLayout>
      </TabViewItem>

      <TabViewItem title="Wallet" iconSource="res://ic_wallet">
        <Wallet />
      </TabViewItem>

      <TabViewItem title="Profile" iconSource="res://ic_profile">
        <Profile />
      </TabViewItem>

    </TabView>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted, getCurrentInstance } from "vue";
import { useKYCStore } from "../stores/kyc.store";
import HomeScreen from "../screens/HomeScreen.vue";
import MyDeliveries from "../screens/MyDeliveries.vue";
import Wallet from "../screens/wallet/Wallet.vue";
import Profile from "../screens/Profile.vue";
import KYCUpload from "../screens/agent/KYCUpload.vue";

// Note: Components are loaded eagerly for NativeScript compatibility
// Future optimization: Implement code splitting with webpack for better performance
const kycStore = useKYCStore();

const instance = getCurrentInstance();
const navigateTo = instance!.proxy!.$navigateTo;

function goToKYC() {
  navigateTo(KYCUpload, {
    props: {
      onDone: async () => { await kycStore.load(); }
    }
  });
}

const tabIndex = ref(0);
onMounted(() => {
  kycStore.load();
});

function onTabChanged(e: any) {
  switch (e.newIndex) {
    case 0:
      (globalThis as any).__homeTabActivated?.();
      break;

    case 1:
      // Reload KYC status when deliveries tab is activated
      kycStore.load();
      (globalThis as any).__agentDeliveriesTabActivated?.();
      break;

    case 2:
      (globalThis as any).__refreshWallet?.();
      break;

    case 3:
      (globalThis as any).__profileTabActivated?.();
      break;
  }
}
</script>
