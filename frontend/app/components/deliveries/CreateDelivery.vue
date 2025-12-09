<template>
    <Page>
        <ActionBar title="Create Delivery" >
        <NavigationButton text="Back" android.systemIcon="ic_menu_back" @tap="$navigateBack()" />
        </ActionBar>
        <ScrollView>
            <StackLayout class="form">

                <Label text="New Delivery" class="title" />

                <TextField v-model="title" hint="Title" class="input" />
                <TextField v-model="description" hint="Description" class="input" />
                <TextField v-model="packageType" hint="Package Type (SMALL/MEDIUM/LARGE)" class="input" />
                <TextField v-model="packageWeight" hint="Weight (kg)" keyboardType="number" class="input" />

                <!-- PICKUP -->
                <Label text="Pickup Address" class="section" />
                <TextField v-model="pickupAddress" hint="Address" class="input" />
                <TextField v-model="pickupContactName" hint="Contact Name" class="input" />
                <TextField v-model="pickupContactPhone" hint="Contact Phone" class="input" />

                <Button text="Use Current Location" class="btn btn-tertiary" @tap="setPickupLocation" />

                <Label v-if="pickupLatitude" :text="'Lat: ' + pickupLatitude" />
                <Label v-if="pickupLongitude" :text="'Lon: ' + pickupLongitude" />

                <!-- DROPOFF -->
                <Label text="Dropoff Address" class="section" />
                <TextField v-model="dropoffAddress" hint="Address" class="input" />
                <TextField v-model="dropoffContactName" hint="Contact Name" class="input" />
                <TextField v-model="dropoffContactPhone" hint="Contact Phone" class="input" />

                <Button text="Use Current Location For Dropoff" class="btn btn-tertiary" @tap="setDropoffLocation" />

                <Label v-if="dropoffLatitude" :text="'Lat: ' + dropoffLatitude" />
                <Label v-if="dropoffLongitude" :text="'Lon: ' + dropoffLongitude" />

                <Button text="Create Delivery" class="btn btn-primary" @tap="submit" :isEnabled="!loading" />

                <ActivityIndicator :busy="loading" />

                <Label v-if="error" :text="error" class="error" />
            </StackLayout>
        </ScrollView>
    </Page>
</template>

<script>
import { createDelivery } from "~/services/deliveries/deliveries.api";
import DeliveryList from "./DeliveryList.vue";

export default {
    data() {
        return {
            title: "",
            description: "",
            packageType: "SMALL",
            packageWeight: 1,

            pickupAddress: "",
            pickupLatitude: null,
            pickupLongitude: null,
            pickupContactName: "",
            pickupContactPhone: "",

            dropoffAddress: "",
            dropoffLatitude: null,
            dropoffLongitude: null,
            dropoffContactName: "",
            dropoffContactPhone: "",

            loading: false,
            error: null
        };
    },

    methods: {
        async setPickupLocation() {
            const loc = await this.getLocation();
            this.pickupLatitude = loc.latitude;
            this.pickupLongitude = loc.longitude;
        },

        async setDropoffLocation() {
            const loc = await this.getLocation();
            this.dropoffLatitude = loc.latitude;
            this.dropoffLongitude = loc.longitude;
        },

        async getLocation() {
            const geo = require("@nativescript/geolocation");

            try {
                const isEnabled = await geo.isEnabled();

                if (!isEnabled) {
                    await geo.enableLocationRequest(true, true);
                }

                const location = await geo.getCurrentLocation({
                    desiredAccuracy: 3,   // high accuracy
                    maximumAge: 5000,
                    timeout: 10000
                });

                if (!location) throw new Error("Could not get location");

                return location;
            } catch (e) {
                alert("Location error: " + e.message);
                throw e;
            }
        },

        async submit() {
            this.loading = true;
            this.error = null;

            const body = {
                title: this.title,
                description: this.description,
                packageType: this.packageType,
                packageWeight: parseFloat(this.packageWeight),

                pickupAddress: this.pickupAddress,
                pickupLatitude: this.pickupLatitude,
                pickupLongitude: this.pickupLongitude,
                pickupContactName: this.pickupContactName,
                pickupContactPhone: this.pickupContactPhone,

                dropoffAddress: this.dropoffAddress,
                dropoffLatitude: this.dropoffLatitude,
                dropoffLongitude: this.dropoffLongitude,
                dropoffContactName: this.dropoffContactName,
                dropoffContactPhone: this.dropoffContactPhone,

                requestedPickupTime: new Date().toISOString(),
                city: "Skopje"
            };

            try {
                await createDelivery(body);
                alert("Delivery created successfully!");
                this.$navigateTo(DeliveryList, { clearHistory: true });
            } catch (e) {
                this.error = e.message;
            } finally {
                this.loading = false;
            }
        }
    }
}
</script>

<style scoped>
.form {
    padding: 20;
}

.title {
    font-size: 22;
    font-weight: bold;
    margin-bottom: 20;
}

.section {
    margin-top: 20;
    font-weight: bold;
}

.input {
    margin-bottom: 10;
    padding: 10;
    background: #fff;
    border-radius: 6;
}

.btn-primary {
    background: #007AFF;
    color: white;
    margin-top: 20;
}

.btn-tertiary {
    background: #eee;
    margin-bottom: 10;
}

.error {
    color: red;
    margin-top: 10;
}
</style>