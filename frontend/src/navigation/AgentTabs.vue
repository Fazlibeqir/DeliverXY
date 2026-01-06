<template>
  <Page>
    <ActionBar title="DeliverXY" />

    <TabView androidTabsPosition="bottom" @selectedIndexChanged="onTabChanged" v-model:selectedIndex="tabIndex">

      <TabViewItem title="Home" iconSource="res://ic_home">
        <HomeScreen />
      </TabViewItem>

      <TabViewItem title="Deliveries" iconSource="res://ic_box">
        <MyDeliveries />
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
import { useDeliveriesStore  } from "../stores/useDeliveryStore";

const deliveriesStore = useDeliveriesStore();
import { ref } from "vue";
const tabIndex = ref(0);

function onTabChanged(e: any) {
  //HomeTab
  if (e.newIndex === 0) {
    (globalThis as any).__homeTabActivated?.();
  }
    // Deliveries tab index = 1
  if (e.newIndex === 1) {
    deliveriesStore.loadAssigned();
  }
  // 👇 ADD THIS
  if (e.newIndex === 2) {
    (globalThis as any).__refreshWallet?.();
  }
  if (e.newIndex === 3) {
    (globalThis as any).__profileTabActivated?.();
  }
}
function goToDeliveries() {
  tabIndex.value = 1;
  deliveriesStore.loadAssigned();
}

</script>
