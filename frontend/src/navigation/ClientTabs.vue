<template>
  <Page>
    <TabView androidTabsPosition="bottom">

      <TabViewItem title="Home">
        <HomeScreen />
      </TabViewItem>

      <TabViewItem title="Deliveries">
        <MyDeliveries />
      </TabViewItem>

      <TabViewItem title="Wallet">
        <Wallet />
      </TabViewItem>

      <TabViewItem title="Profile">
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
