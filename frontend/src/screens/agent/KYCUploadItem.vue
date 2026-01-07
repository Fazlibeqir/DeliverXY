<template>
    <StackLayout class="card p-3 flex-row items-center justify-between" :opacity="disabled ? 0.5 : 1"
        @tap="!done && !loading && !disabled && $emit('tap')">
        <Label :text="label" class="font-bold" />


        <Label v-if="done" text="✔ Uploaded" class="text-green-600" />

        <ActivityIndicator v-else-if="loading" busy="true" width="20" height="20" />
        <Label v-else-if="disabled" text="Locked while under review" class="text-gray-400" />


        <Label v-else text="Tap to upload" class="text-gray-400" />
    </StackLayout>
</template>

<script setup lang="ts">
defineProps<{
    label: string;
    done: boolean;
    loading: boolean;
    disabled?: boolean;
}>();
</script>