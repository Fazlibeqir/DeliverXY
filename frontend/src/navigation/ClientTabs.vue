<template>
  <Page>
    <ActionBar title="DeliverXY" />

    <TabView androidTabsPosition="bottom" @selectedIndexChanged="onTabChanged" v-model:selectedIndex="tabIndex">

      <TabViewItem title="Home" iconSource="res://ic_home">
        <ClientHome />
      </TabViewItem>

      <TabViewItem title="Deliveries" iconSource="res://ic_box">
        <ClientDeliveries />
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
import ClientHome from "../screens/client/ClientHome.vue";
import ClientDeliveries from "../screens/client/ClientDeliveries.vue";
import Wallet from "../screens/wallet/Wallet.vue";
import Profile from "../screens/Profile.vue";

import { ref } from "vue";

const tabIndex = ref(0);

function onTabChanged(e: any) {
  switch (e.newIndex) {
    case 0:
      (globalThis as any).__homeTabActivated?.();
      break;

    case 1:
      (globalThis as any).__clientDeliveriesTabActivated?.();
      break;

    case 2:
      (globalThis as any).__walletTabActivated?.();
      break;

    case 3:
      (globalThis as any).__profileTabActivated?.();
      break;
  }
}
</script>
