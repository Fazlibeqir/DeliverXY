<template>
    <Page>
        <ActionBar title="Delivery Details" />

        <ScrollView>
            <StackLayout class="container">

                <ActivityIndicator :busy="loading" />

                <StackLayout v-if="delivery">

                    <Label :text="delivery.title" class="title" />

                    <Label :text="'Status: ' + delivery.status" class="status" />

                    <Label text="Pickup" class="section" />
                    <Label :text="delivery.pickupAddress" />
                    <Label :text="'Contact: ' + delivery.pickupContactName" />
                    <Label :text="'Phone: ' + delivery.pickupContactPhone" />

                    <Label text="Dropoff" class="section" />
                    <Label :text="delivery.dropoffAddress" />
                    <Label :text="'Contact: ' + delivery.dropoffContactName" />
                    <Label :text="'Phone: ' + delivery.dropoffContactPhone" />

                    <Label text="Times" class="section" />
                    <Label :text="'Requested Pickup: ' + delivery.requestedPickupTime" />
                    <Label :text="'Created: ' + delivery.createdAt" />

                    <Button text="Track Delivery" class="btn btn-primary" @tap="goTrack" />
                </StackLayout>

            </StackLayout>
        </ScrollView>
    </Page>
</template>

<script>
import { getDelivery } from "~/services/deliveries/deliveries.api";

export default {
    props: ["deliveryId"],

    data() {
        return {
            delivery: null,
            loading: false
        };
    },

    async mounted() {
        this.loading = true;
        try {
            this.delivery = await getDelivery(this.deliveryId);
        } finally {
            this.loading = false;
        }
    },

    methods: {
        goTrack() {
            alert("Tracking screen soon...");
        }
    }
};
</script>

<style scoped>
.container {
    padding: 20;
}

.title {
    font-size: 22;
    font-weight: bold;
    margin-bottom: 10;
}

.status {
    margin-bottom: 20;
    font-size: 16;
}

.section {
    margin-top: 20;
    font-weight: bold;
    font-size: 18;
}

.btn-primary {
    background-color: #007AFF;
    color: white;
    margin-top: 20;
}
</style>