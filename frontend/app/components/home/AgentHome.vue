<template>
    <Page>
        <ActionBar title="DeliverXY – Agent" />

        <ScrollView>
            <StackLayout class="container">

                <!-- HEADER -->
                <Label :text="'Welcome, ' + (user?.firstName || '')" class="title" />

                <!-- EARNINGS CARD -->
                <StackLayout class="card" @tap="goEarnings">
                    <Label text="Earnings (Last 30 Days)" class="card-title" />
                    <Label :text="earningsDisplay" class="card-value" />
                </StackLayout>

                <!-- LOCATION UPDATE -->
                <Button text="Update My Location" class="btn-primary" @tap="sendLocation" />

                <!-- QUICK ACTIONS -->
                <GridLayout columns="*,*" rows="auto,auto" class="quick-actions">
                    <Button text="My Deliveries" @tap="goMyDeliveries" row="0" col="0" />

                    <Button text="Vehicles" @tap="goVehicles" row="0" col="1" />

                    <Button text="KYC" @tap="goKYC" row="1" col="0" />

                    <Button text="Notifications" @tap="goNotifications" row="1" col="1" />
                </GridLayout>

                <!-- ASSIGNED DELIVERIES -->
                <Label text="Assigned Deliveries" class="section-title" />

                <ActivityIndicator :busy="loadingDeliveries" />

                <Label v-if="!loadingDeliveries && deliveries.length === 0" text="No active deliveries." />

                <ListView for="item in deliveries" @itemTap="openDelivery">
                    <v-template>
                        <StackLayout class="delivery-item">
                            <Label :text="'Delivery #' + item.id" class="id" />
                            <Label :text="'Status: ' + item.status" />
                            <Label :text="'Pickup: ' + item.senderAddress" />
                            <Label :text="'Dropoff: ' + item.receiverAddress" />
                        </StackLayout>
                    </v-template>
                </ListView>

            </StackLayout>
        </ScrollView>
    </Page>
</template>

<script>
import { getMe } from "~/services/auth/auth.api";
import { summary as getEarningsSummary } from "~/services/earnings/earnings.api";
import { updateLocation } from "~/services/agent/agent.api";
import { getMyDeliveries } from "~/services/deliveries/deliveries.api";

export default {
    data() {
        return {
            user: null,
            earnings: null,
            deliveries: [],
            loadingDeliveries: false
        };
    },

    mounted() {
        this.loadUser();
        this.loadEarnings();
        this.loadDeliveries();
    },

    methods: {
        async loadUser() {
            this.user = await getMe();
        },

        async loadEarnings() {
            this.earnings = await getEarningsSummary();
        },

        get earningsDisplay() {
            if (!this.earnings) return "...";
            return this.earnings.total + " ден";
        },

        async loadDeliveries() {
            this.loadingDeliveries = true;
            try {
                this.deliveries = await getMyDeliveries();
            } finally {
                this.loadingDeliveries = false;
            }
        },

        async sendLocation() {
            try {
                const loc = await this.getCurrentLocation();
                await updateLocation(loc.latitude, loc.longitude);
                alert("Location updated");
            } catch (e) {
                alert(e.message);
            }
        },

        getCurrentLocation() {
            return new Promise((resolve, reject) => {
                const geolocation = require("@nativescript/geolocation");
                geolocation.enableLocationRequest().then(() => {
                    geolocation.getCurrentLocation({}).then(resolve).catch(reject);
                });
            });
        },

        goMyDeliveries() { },
        goVehicles() { },
        goKYC() { },
        goNotifications() { },
        goEarnings() { },

        openDelivery(args) {
            const item = args.item;
            // navigate to DeliveryDetails.vue
        }
    }
};
</script>

<style scoped>
.container {
    padding: 20;
}

.title {
    font-size: 24;
    margin-bottom: 20;
    font-weight: bold;
}

.card {
    padding: 15;
    background: #fff;
    border-radius: 10;
    margin-bottom: 20;
}

.card-title {
    font-size: 16;
    color: #666;
}

.card-value {
    font-size: 22;
    font-weight: bold;
    margin-top: 5;
}

.quick-actions {
    margin-bottom: 20;
}

.btn-primary {
    background: #007AFF;
    color: white;
    margin-bottom: 15;
}

.section-title {
    font-size: 18;
    margin: 20 0 10 0;
    font-weight: bold;
}

.delivery-item {
    padding: 12;
    margin-bottom: 8;
    border-radius: 8;
    background: white;
}

.id {
    font-size: 16;
    font-weight: bold;
}
</style>