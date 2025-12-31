<template>
  <StackLayout class="p-4">
    <Label text="👋 Welcome back" class="text-xl font-bold mb-2" />
    <Label text="Ready to start earning?" class="text-gray-500 mb-4" />

    <GridLayout columns="*,*" class="gap-3">
      <StackLayout class="card">
        <Label text="Nearby" class="font-bold" />
        <Label text="Find deliveries" class="text-gray-500" />
      </StackLayout>

      <StackLayout class="card">
        <Label text="Status" class="font-bold" />
        <Label text="Online" class="text-green-600" />
      </StackLayout>
    </GridLayout>
  </StackLayout>
</template>
