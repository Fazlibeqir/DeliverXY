<template>
  <Page>
    <ActionBar title="DeliverXY" />

    <TabView androidTabsPosition="bottom" @selectedIndexChanged="onTabChanged" v-model:selectedIndex="tabIndex">

      <TabViewItem title="Home" iconSource="res://ic_home">
        <HomeScreen />
      </TabViewItem>

      <TabViewItem title="Deliveries" iconSource="res://ic_box">
        <GridLayout rows="*" columns="*">

          <!-- Actual deliveries -->
          <MyDeliveries row="0" col="0" v-show="kycStore.status === 'APPROVED'" />

          <!-- Blocker message -->
          <StackLayout row="0" col="0" v-show="kycStore.status !== 'APPROVED'" class="p-4">
            <Label text="You must complete KYC to accept deliveries." class="text-gray-500 text-center" />
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
import { ref, onMounted } from "vue";
import { useKYCStore } from "../stores/kyc.store";
const kycStore = useKYCStore();

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
