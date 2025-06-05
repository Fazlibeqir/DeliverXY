<template>
    <Page class="page">
      <ActionBar title="DeliverXY" class="action-bar" />
      <StackLayout class="home-panel" verticalAlignment="center" horizontalAlignment="center">
        <Label 
        v-for="delivery in deliveries"
        :key="delivery.id"
        :text="delivery.description" />
      </StackLayout>
    </Page>
  </template>

  
  
<script>
export default {
  data() {
    return {
      deliveries: [],
    };
  },
  mounted() {
    fetch("http://10.0.2.2:8080/api/deliveries")
      .then((res) => res.json())
      .then((data) => {
        this.deliveries = data;
      })
      .catch((err) => console.error(err));
  },
};
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
  