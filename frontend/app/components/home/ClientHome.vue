<template>
    <Page>
        <ActionBar title="DeliverXY – Client" />

        <ScrollView>
            <StackLayout class="container">

                <!-- USER HEADER -->
                <Label :text="'Hello, ' + (user?.firstName || '')" class="title" />

                <!-- WALLET CARD -->
                <StackLayout class="card" @tap="goWallet">
                    <Label text="Wallet Balance" class="card-title" />
                    <Label :text="walletDisplay" class="card-value" />
                </StackLayout>

                <!-- QUICK ACTIONS -->
                <GridLayout columns="*,*" rows="auto,auto" class="quick-actions">

                    <Button text="Create Delivery" @tap="goCreateDelivery" row="0" col="0" class="btn-primary" />

                    <Button text="My Deliveries" @tap="goMyDeliveries" row="0" col="1" />

                    <Button text="Promo Codes" @tap="goPromo" row="1" col="0" />

                    <Button text="KYC" @tap="goKYC" row="1" col="1" />
                </GridLayout>

                <!-- RECENT DELIVERIES -->
                <Label text="Recent Deliveries" class="section-title" />

                <ActivityIndicator :busy="loadingDeliveries" />

                <Label v-if="!loadingDeliveries && deliveries.length === 0" text="No deliveries found." />

                <ListView for="item in deliveries" @itemTap="openDelivery">
                    <v-template>
                        <StackLayout class="delivery-item">
                            <Label :text="'#' + item.id" class="id" />
                            <Label :text="'Status: ' + item.status" />
                            <Label :text="'To: ' + item.receiverAddress" />
                        </StackLayout>
                    </v-template>
                </ListView>

            </StackLayout>
        </ScrollView>
    </Page>
</template>

<script>
import { getMe } from "~/services/auth/auth.api";
import { getWallet } from "~/services/wallet/wallet.api";
import { getMyDeliveries } from "~/services/deliveries/deliveries.api";

export default {
    data() {
        return {
            user: null,
            wallet: null,
            deliveries: [],
            loadingDeliveries: false
        };
    },

    mounted() {
        this.loadUser();
        this.loadWallet();
        this.loadDeliveries();
    },

    methods: {
        async loadUser() {
            this.user = await getMe();
        },

        async loadWallet() {
            this.wallet = await getWallet();
        },

        async loadDeliveries() {
            this.loadingDeliveries = true;
            try {
                let result = await getMyDeliveries();
                this.deliveries = result;
            } finally {
                this.loadingDeliveries = false;
            }
        },

        get walletDisplay() {
            if (!this.wallet) return "...";
            return this.wallet.balance + " ден";
        },

        goCreateDelivery() {
            // navigate to delivery creation screen
        },

        goMyDeliveries() {
            // navigate to deliveries list
        },

        goPromo() {
            // navigate to promo codes
        },

        goKYC() {
            // navigate to KYC upload/status
        },

        openDelivery(args) {
            const d = args.item;
            // navigate to DeliveryDetails.vue
        },

        goWallet() {
            // navigate to wallet screen
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