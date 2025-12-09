<template>
    <Page>
        <ActionBar :title="isAgent ? 'My Assigned Deliveries' : 'My Deliveries'" />

        <StackLayout class="container">

            <ActivityIndicator :busy="loading" />

            <Label v-if="!loading && deliveries.length === 0" text="No deliveries found" class="empty" />

            <ListView v-if="!loading" for="item in deliveries" @itemTap="openDelivery">
                <v-template>
                    <StackLayout class="item">
                        <Label :text="'#' + item.id" class="id" />
                        <Label :text="item.title" class="title" />
                        <Label :text="'Status: ' + item.status" class="status" />
                        <Label :text="'To: ' + item.dropoffAddress" class="address" />
                    </StackLayout>
                </v-template>
            </ListView>

        </StackLayout>
    </Page>
</template>

<script>
import { getMe } from "~/services/auth/auth.api";
import { getMyDeliveries, getAssignedDeliveries } from "~/services/deliveries/deliveries.api";
import DeliveryDetails from "./DeliveryDetails.vue";

export default {
    data() {
        return {
            user: null,
            deliveries: [],
            loading: false
        };
    },

    async mounted() {
        this.user = await getMe();
        await this.load();
    },

    computed: {
        isAgent() {
            return this.user.role === "Agent" || this.user.role === "AGENT";
        }
    },

    methods: {
        async load() {
            this.loading = true;
            try {
                this.deliveries = this.isAgent
                    ? await getAssignedDeliveries()
                    : await getMyDeliveries();
            } finally {
                this.loading = false;
            }
        },

        openDelivery(args) {
            this.$navigateTo(DeliveryDetails, {
                props: { deliveryId: args.item.id }
            });
        }
    }
};
</script>

<style scoped>
.container {
    padding: 20;
}

.empty {
    text-align: center;
    margin-top: 30;
    color: #777;
}

.item {
    background: #fff;
    padding: 15;
    margin-bottom: 10;
    border-radius: 8;
}

.id {
    font-weight: bold;
    font-size: 18;
}

.title {
    font-size: 16;
    margin-top: 6;
}

.status {
    margin-top: 4;
    color: #333;
}

.address {
    color: #777;
    font-size: 14;
    margin-top: 4;
}
</style>