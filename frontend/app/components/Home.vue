<template>
  <Page class="page">
    <ActionBar title="DeliverXY" class="action-bar" />
    <StackLayout class="home-panel" verticalAlignment="center" horizontalAlignment="center">
      
      <ActivityIndicator v-if="loading" :busy="loading" />
      
      <Label v-else-if="error" :text="'Error: ' + error" class="h2 error-msg" textWrap="true" />

      <StackLayout v-else>
        <Label class="h1" text="My Latest Deliveries" />
        
        <ListView class="list-view" for="delivery in deliveries" @itemTap="onItemTap">
          <v-template>
            <StackLayout class="delivery-item">
              <Label :text="'ID: ' + delivery.id" class="id-label" />
              <Label :text="'Status: ' + delivery.status" class="status-label" />
              <Label :text="'From: ' + delivery.senderAddress" class="address-label" />
              <Label :text="'To: ' + delivery.receiverAddress" class="address-label" />
            </StackLayout>
          </v-template>
        </ListView>
        
        <Label v-if="deliveries.length === 0" text="No deliveries found." class="h2" />
      </StackLayout>
      
    </StackLayout>
  </Page>
</template>

<script>
// Import the conceptual API service
import { apiFetch } from '~/services/core/api-fetch'; 
// NOTE: Adjust the path based on your project structure

export default {
  data() {
    return {
      deliveries: [],
      loading: false,
      error: null,
    };
  },
  mounted() {
    this.fetchDeliveries();
  },
  methods: {
    async fetchDeliveries() {
      this.loading = true;
      this.error = null;

      try {
        // Fetch current user's deliveries (assuming /api/deliveries/me is the correct endpoint)
        // If /api/deliveries is used, the backend needs to handle ownership based on the JWT token.
        // We'll use the authenticated endpoint for better security and filtering.
        const data = await apiFetch("/deliveries/me"); 
        
        // Assuming the data returned is a List or Page of DeliveryResponseDTO objects
        this.deliveries = data.content || data; 
        
      } catch (err) {
        this.error = err.message || "Failed to load deliveries.";
      } finally {
        this.loading = false;
      }
    },
    onItemTap(args) {
      const delivery = args.item;
      // Navigate to a detail page
      console.log(`Tapped delivery: ${delivery.id}`);
      // Example navigation: this.$navigateTo(DeliveryDetails, { props: { deliveryId: delivery.id } });
    }
  }
};
</script>

<style scoped>
.page {
  background-color: #f0f0f0;
}
.home-panel {
  padding: 20;
  flex-grow: 1; /* Allow the layout to grow */
}
.h1 {
  font-weight: bold;
  font-size: 24;
  margin-bottom: 20;
  text-align: center;
}
.h2 {
  font-size: 18;
  color: #333;
  text-align: center;
}
.error-msg {
  color: red;
  font-weight: bold;
}
.list-view {
  /* Ensure ListView takes available space */
  height: 100%; 
}
.delivery-item {
  padding: 15;
  margin-bottom: 8;
  background-color: white;
  border-radius: 8;
  border-color: #ccc;
  border-width: 1;
}
.id-label {
  font-weight: bold;
  color: #007aff; /* Blue accent */
}
.status-label {
  font-size: 16;
  margin-top: 5;
}
.address-label {
  font-size: 14;
  color: #666;
}
</style>