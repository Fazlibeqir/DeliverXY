<template>
  <Page>
    <ActionBar title="DeliverXY" />

    <TabView androidTabsPosition="bottom">

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
import Wallet from "../screens/Wallet.vue";
import Profile from "../screens/Profile.vue";
</script>
