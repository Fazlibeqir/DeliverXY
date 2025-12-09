<template>
    <StackLayout>
  
      <!-- REQUESTED → Accept -->
      <Button v-if="status === 'REQUESTED'"
              text="Accept Delivery"
              class="btn btn-primary"
              @tap="accept" />
  
      <!-- ASSIGNED → Picked Up -->
      <Button v-if="status === 'ASSIGNED'"
              text="Mark as Picked Up"
              class="btn btn-primary"
              @tap="pickedUp" />
  
      <!-- PICKED_UP → Start Delivery -->
      <Button v-if="status === 'PICKED_UP'"
              text="Start Delivery"
              class="btn btn-primary"
              @tap="startTransit" />
  
      <!-- IN_TRANSIT → Complete -->
      <Button v-if="status === 'IN_TRANSIT'"
              text="Complete Delivery"
              class="btn btn-primary"
              @tap="complete" />
  
      <!-- Send live location while not completed -->
      <Button v-if="status !== 'DELIVERED' && status !== 'CANCELLED'"
              text="Send My Location"
              class="btn btn-secondary"
              @tap="sendLocation" />
  
    </StackLayout>
  </template>
  
  <script>
  import {
    assignDelivery,
    updateDeliveryStatus,
    updateDeliveryLocation
  } from "~/services/agent/agent.api";
  
  export default {
    props: ["deliveryId", "status"],
  
    methods: {
      async accept() {
        await assignDelivery(this.deliveryId);
        alert("Delivery accepted!");
        this.$emit("updated");
      },
  
      async pickedUp() {
        await updateDeliveryStatus(this.deliveryId, "PICKED_UP");
        alert("Marked as picked up.");
        this.$emit("updated");
      },
  
      async startTransit() {
        await updateDeliveryStatus(this.deliveryId, "IN_TRANSIT");
        alert("Delivery is now in transit.");
        this.$emit("updated");
      },
  
      async complete() {
        await updateDeliveryStatus(this.deliveryId, "DELIVERED");
        alert("Delivery completed.");
        this.$emit("updated");
      },
  
      async sendLocation() {
        const geo = require("@nativescript/geolocation");
  
        await geo.enableLocationRequest();
        const loc = await geo.getCurrentLocation({ timeout: 5000 });
  
        await updateDeliveryLocation(
          this.deliveryId,
          loc.latitude,
          loc.longitude
        );
  
        alert("Location sent.");
      }
    }
  }
  </script>
  
  <style>
  .btn-primary { background: #007AFF; color: white; margin: 10 0; }
  .btn-secondary { background: #ccc; margin: 10 0; }
  </style>
  