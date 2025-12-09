<template>
    <Page>
        <ActionBar :title="'Delivery #' + deliveryId" />

        <ScrollView>
            <StackLayout class="container">

                <ActivityIndicator :busy="loading" />

                <Label v-if="error" :text="error" class="error" />

                <StackLayout v-if="delivery">

                    <Label :text="'Status: ' + delivery.status" class="status" />

                    <Label :text="'Title: ' + delivery.title" class="label" />
                    <Label :text="'Description: ' + delivery.description" class="label" />

                    <Label :text="'Pickup: ' + delivery.pickupAddress" class="label" />
                    <Label :text="'Dropoff: ' + delivery.dropoffAddress" class="label" />

                    <Label :text="'Package: ' + delivery.packageType + ' (' + delivery.packageWeight + 'kg)'"
                        class="label" />

                    <Label :text="'Requested Pickup: ' + delivery.requestedPickupTime" class="label" />

                    <!-- Agent action buttons -->
                    <AgentDeliveryActions v-if="isAgent" :deliveryId="deliveryId" :status="delivery.status"
                        @updated="loadDelivery" />

                </StackLayout>

            </StackLayout>
        </ScrollView>
    </Page>
</template>

<script>
import { getDeliveryById } from "~/services/deliveries/deliveries.api";
import AgentDeliveryActions from "./AgentDeliveryActions.vue";
import { getMe } from "~/services/auth/auth.api";

export default {
    components: { AgentDeliveryActions },

    props: ["deliveryId"],

    data() {
        return {
            delivery: null,
            loading: false,
            error: null,
            user: null
        };
    },

    async mounted() {
        this.user = await getMe();
        await this.loadDelivery();
    },

    computed: {
        isAgent() {
            return this.user?.role === "Agent" || this.user?.role === "AGENT";
        }
    },

    methods: {
        async loadDelivery() {
            this.loading = true;
            this.error = null;

            try {
                this.delivery = await getDeliveryById(this.deliveryId);
            } catch (e) {
                this.error = e.message;
            } finally {
                this.loading = false;
            }
        }
    }
};
</script>

<style scoped>
.container {
    padding: 20;
}

.label {
    margin-bottom: 8;
}

.status {
    font-size: 18;
    font-weight: bold;
    margin-bottom: 10;
}

.error {
    color: red;
}
</style>