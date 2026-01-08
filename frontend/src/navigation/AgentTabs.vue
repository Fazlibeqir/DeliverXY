<template>
  <Page>
    <ActionBar title="DeliverXY" />

    <TabView androidTabsPosition="bottom" @selectedIndexChanged="onTabChanged" v-model:selectedIndex="tabIndex">

      <TabViewItem title="Home" iconSource="res://ic_home">
        <HomeScreen />
      </TabViewItem>

      <TabViewItem title="Deliveries" iconSource="res://ic_box">
        <GridLayout rows="*" columns="*">
          <MyDeliveries row="0" col="0" v-show="kycStore.status === 'APPROVED'" />
          <StackLayout row="0" col="0" v-show="kycStore.status !== 'APPROVED'" class="empty-state">
            <Label text="🔒" class="empty-state-icon" />
            <Label text="KYC Verification Required" class="text-xl font-bold mb-2" />
            <Label text="You must complete KYC verification to accept deliveries." class="empty-state-text mb-4" />
            <Button text="Complete KYC" class="btn-primary" @tap="goToKYC" />
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
import HomeScreen from "../screens/HomeScreen.vue";
import MyDeliveries from "../screens/MyDeliveries.vue";
import Wallet from "../screens/wallet/Wallet.vue";
import Profile from "../screens/Profile.vue";
import KYCUpload from "../screens/agent/KYCUpload.vue";
import { ref, onMounted, getCurrentInstance } from "vue";
import { useKYCStore } from "../stores/kyc.store";
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
