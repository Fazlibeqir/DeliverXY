<template>
    <Page class="page">
      <ActionBar title="DeliverXY" class="action-bar" />
      <StackLayout class="home-panel" verticalAlignment="center" horizontalAlignment="center">
        <Label text="Welcome to DeliverXY" class="h1" />
        <Button text="View Deliveries" @tap="onViewDeliveries" />
      </StackLayout>
    </Page>
  </template>

  
  
<script>
import { Http } from "@nativescript/core";

export default {
  data() {
    return {
      deliveries: []
    };
  },
  methods: {
    async onViewDeliveries() {
      try {
        const response = await Http.request({
          url: "http://10.0.2.2:8080/api/deliveries", // Replace with your backend IP
          method: "GET"
        });
        this.deliveries = response.content.toJSON();
        alert('Deliveries:\n' + this.deliveries.map(d => d.name || d).join('\n'));
      } catch (err) {
        console.error(err);
        alert("Failed to fetch deliveries");
      }
    }
  }
}
</script>


  
  <style scoped>
  .page {
    background-color: #f0f0f0;
  }
  .home-panel {
    padding: 20;
  }
  .h1 {
    font-weight: bold;
    font-size: 24;
    margin-bottom: 20;
  }
  </style>
  