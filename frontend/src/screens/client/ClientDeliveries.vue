<template>
  <GridLayout rows="auto, *">
    <!-- Search and Filter Bar -->
    <StackLayout row="0" class="p-4" style="background-color: #FFFFFF; border-bottom-width: 1; border-bottom-color: #e0e0e0;">
      <!-- Search Input -->
      <StackLayout class="mb-3">
        <TextField 
          v-model="searchQuery" 
          hint="🔍 Search deliveries..." 
          class="input"
          @textChange="onSearchChange"
        />
      </StackLayout>
      
      <!-- Filter Chips -->
      <ScrollView orientation="horizontal" scrollBarIndicatorVisible="false" class="mb-2">
        <StackLayout orientation="horizontal">
          <Button
            :text="`All (${filteredDeliveries.length})`"
            :class="selectedStatus === 'ALL' ? 'btn-primary-compact' : 'btn-outline-compact'"
            style="margin-right: 8;"
            @tap="setStatusFilter('ALL')"
          />
          <Button
            v-for="status in statusFilters"
            :key="status.value"
            :text="`${status.label} (${getStatusCount(status.value)})`"
            :class="selectedStatus === status.value ? 'btn-primary-compact' : 'btn-outline-compact'"
            style="margin-right: 8;"
            @tap="setStatusFilter(status.value)"
          />
        </StackLayout>
      </ScrollView>
    </StackLayout>

    <ScrollView row="1">
      <StackLayout class="p-4">
      <!-- Header -->
      <StackLayout class="mb-4">
        <Label text="📦 My Deliveries" class="section-header mb-1" />
        <Label text="Track and manage your delivery requests" class="text-secondary text-sm" />
      </StackLayout>

      <!-- Loading State -->
      <SkeletonLoader v-if="store.loading" type="list" :count="3" />

      <!-- Delivery Cards -->
      <StackLayout
        v-for="d in paginatedDeliveries"
        :key="d.id"
        class="card-elevated mb-4 p-4"
        :style="getCardStyle(d.status)"
      >
        <!-- Header with Title and Status -->
        <StackLayout class="mb-3">
          <StackLayout class="mb-2" orientation="horizontal">
            <Label :text="d.title || 'Untitled Delivery'" class="font-bold text-lg" style="flex-grow: 1;" />
            <Label :text="getStatusLabel(d.status)" :class="getStatusClass(d.status)" class="status-badge" />
          </StackLayout>
          
          <StackLayout v-if="d.description" class="mb-2">
            <Label :text="d.description" class="text-sm text-secondary" />
          </StackLayout>
        </StackLayout>

        <StackLayout class="divider mb-3" />

        <!-- Delivery Address -->
        <StackLayout class="mb-3">
          <Label text="📍 Delivery Address" class="text-xs text-secondary mb-1" />
          <Label :text="d.dropoffAddress" class="text-sm font-semibold" />
        </StackLayout>

        <!-- Agent Information -->
        <StackLayout v-if="d.agentUsername" class="mb-3">
          <StackLayout class="divider mb-3" />
          <StackLayout orientation="horizontal">
            <Label text="🚚" class="mr-2" />
            <StackLayout style="flex-grow: 1;">
              <Label text="Assigned Agent" class="text-xs text-secondary mb-1" />
              <Label :text="d.agentUsername" class="text-sm font-semibold" />
            </StackLayout>
          </StackLayout>
        </StackLayout>

        <!-- Delivery ID -->
        <StackLayout class="mb-3">
          <Label :text="`Tracking: #${d.id}`" class="text-xs text-secondary" />
        </StackLayout>

        <!-- Actions -->
        <StackLayout v-if="d.status === 'DELIVERED'" class="mt-3">
          <StackLayout class="divider mb-3" />
          <Label v-if="rated[d.id]" text="✓ You rated this delivery" class="text-success text-sm text-center mb-2" />
          <Button
            v-if="!rated[d.id]"
            text="⭐ Rate Delivery"
            class="btn-primary-compact"
            @tap="rate(d.id)"
          />
        </StackLayout>
      </StackLayout>

      <!-- Empty State -->
      <StackLayout
        v-if="!store.loading && filteredDeliveries.length === 0"
        class="empty-state"
      >
        <Label text="📦" class="empty-state-icon" />
        <Label 
          :text="searchQuery || selectedStatus !== 'ALL' ? 'No deliveries match your filters' : 'No deliveries yet'" 
          class="empty-state-text font-bold" 
        />
        <Label 
          :text="searchQuery || selectedStatus !== 'ALL' ? 'Try adjusting your search or filters' : 'Create your first delivery from the home screen'" 
          class="text-secondary text-sm mt-2" 
        />
      </StackLayout>

      <!-- Pagination Controls -->
      <StackLayout 
        v-if="!store.loading && filteredDeliveries.length > itemsPerPage"
        class="mt-4"
        orientation="horizontal"
      >
        <Button
          text="← Previous"
          :isEnabled="currentPage > 1"
          class="btn-outline"
          style="flex-grow: 1; margin-right: 8;"
          @tap="prevPage"
        />
        <Label 
          :text="`Page ${currentPage} of ${totalPages}`" 
          class="text-center"
          style="flex-grow: 1; padding: 12;"
        />
        <Button
          text="Next →"
          :isEnabled="currentPage < totalPages"
          class="btn-outline"
          style="flex-grow: 1; margin-left: 8;"
          @tap="nextPage"
        />
      </StackLayout>

    </StackLayout>
    </ScrollView>
  </GridLayout>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from "vue";
import { useClientDeliveriesStore } from "../../stores/useDeliveryStore";
import SkeletonLoader from "../../components/SkeletonLoader.vue";
import RateDelivery from "../ratings/RateDelivery.vue";
import { $navigateTo } from "nativescript-vue";
import * as RatingsService from "@/services/ratings.service";
import { authStore } from "@/stores/auth.store";
import { debounce } from "@/utils/debounce";
import type { DeliveryStatus } from "@/services/deliveries.service";

const store = useClientDeliveriesStore();
const rated = ref<Record<number, boolean>>({});
const searchQuery = ref("");
const selectedStatus = ref<DeliveryStatus | "ALL">("ALL");

// Pagination
const itemsPerPage = ref(10);
const currentPage = ref(1);

const statusFilters = [
  { value: "PENDING" as DeliveryStatus, label: "Pending" },
  { value: "ASSIGNED" as DeliveryStatus, label: "Assigned" },
  { value: "PICKED_UP" as DeliveryStatus, label: "Picked Up" },
  { value: "IN_TRANSIT" as DeliveryStatus, label: "In Transit" },
  { value: "DELIVERED" as DeliveryStatus, label: "Delivered" },
  { value: "CANCELLED" as DeliveryStatus, label: "Cancelled" },
];

// Filtered deliveries based on search and status
const filteredDeliveries = computed(() => {
  let filtered = [...store.list];
  
  // Filter by status
  if (selectedStatus.value !== "ALL") {
    filtered = filtered.filter(d => d.status === selectedStatus.value);
  }
  
  // Filter by search query
  if (searchQuery.value.trim().length > 0) {
    const query = searchQuery.value.toLowerCase().trim();
    filtered = filtered.filter(d => {
      const title = (d.title || "").toLowerCase();
      const description = (d.description || "").toLowerCase();
      const pickupAddress = (d.pickupAddress || "").toLowerCase();
      const dropoffAddress = (d.dropoffAddress || "").toLowerCase();
      const id = d.id?.toString() || "";
      
      return title.includes(query) ||
             description.includes(query) ||
             pickupAddress.includes(query) ||
             dropoffAddress.includes(query) ||
             id.includes(query);
    });
  }
  
  return filtered;
});

// Paginated deliveries
const paginatedDeliveries = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredDeliveries.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.ceil(filteredDeliveries.value.length / itemsPerPage.value);
});

function setStatusFilter(status: DeliveryStatus | "ALL") {
  selectedStatus.value = status;
  currentPage.value = 1; // Reset to first page when filter changes
}

function onSearchChange() {
  currentPage.value = 1; // Reset to first page when search changes
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
}

function getStatusCount(status: DeliveryStatus | "ALL"): number {
  if (status === "ALL") return store.list.length;
  return store.list.filter(d => d.status === status).length;
}

onMounted(() => {
  (globalThis as any).__clientDeliveriesTabActivated = () => {
    store.loadMine(true);
    loadRatedFlags();
  };
  (globalThis as any).__deliveryRated = (deliveryId: number) => {
    rated.value[deliveryId] = true;
  };
  loadRatedFlags();
});

onUnmounted(() => {
  delete (globalThis as any).__clientDeliveriesTabActivated;
  delete (globalThis as any).__deliveryRated;
});

function rate(deliveryId: number) {
  $navigateTo(RateDelivery, { props: { deliveryId } });
}

async function loadRatedFlags() {
  const me = authStore.user?.id;
  if (!me) return;
  const delivered = store.list.filter((d: any) => d.status === "DELIVERED");
  for (const d of delivered) {
    if (rated.value[d.id]) continue;
    try {
      const list = await RatingsService.getDeliveryRatings(d.id);
      const mine = (list || []).some((r: any) => {
        const rid = r?.reviewer?.id ?? r?.reviewerId;
        return rid === me;
      });
      if (mine) rated.value[d.id] = true;
    } catch {}
  }
}

function getStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    PENDING: "Pending",
    ASSIGNED: "Assigned",
    PICKED_UP: "Picked Up",
    IN_TRANSIT: "In Transit",
    DELIVERED: "Delivered",
    CANCELLED: "Cancelled",
  };
  return labels[status] || status;
}

function getStatusClass(status: string): string {
  const classes: Record<string, string> = {
    PENDING: "status-pending",
    ASSIGNED: "status-assigned",
    PICKED_UP: "status-picked-up",
    IN_TRANSIT: "status-in-transit",
    DELIVERED: "status-delivered",
    CANCELLED: "status-cancelled",
  };
  return classes[status] || "";
}

function getCardStyle(status: string): string {
  // Add subtle left border to distinguish cards
  const borderColors: Record<string, string> = {
    PENDING: "#FFC107",
    ASSIGNED: "#17A2B8",
    PICKED_UP: "#6C757D",
    IN_TRANSIT: "#0DCAF0",
    DELIVERED: "#198754",
    CANCELLED: "#DC3545",
  };
  const color = borderColors[status] || "#CCCCCC";
  return `border-left-width: 4; border-left-color: ${color};`;
}
</script>
