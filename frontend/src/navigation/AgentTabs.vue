<template>
  <Page>
    <ActionBar title="DeliverXY" />

    <TabView androidTabsPosition="bottom">

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
import Wallet from "../screens/Wallet.vue";
import Profile from "../screens/Profile.vue";
</script>
