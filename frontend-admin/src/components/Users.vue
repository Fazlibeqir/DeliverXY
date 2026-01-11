<script setup>
import api from '../services/axios'
import { ref, onMounted } from 'vue'

const users = ref([])
const newUser = ref({ username:'', email:'', password:'', role:'USER' })

async function fetchUsers() {
  users.value = (await api.get('/user')).data
}

async function createUser() {
  try {
    await api.post('/user/add-user', newUser.value)
    newUser.value = { username:'', email:'', password:'', role:'USER' }
    fetchUsers()
  } catch (error) {
    console.error('Create user failed:', error.response?.data || error.message)
  }
}

async function updateUser(u) {
  const updated = { username:u.username, email:u.email, password:'', phoneNumber:u.phoneNumber, firstName:u.firstName, lastName:u.lastName, role:u.role }
  await api.post(`/user/edit-user/${u.id}`, updated)
  fetchUsers()
}

async function deleteUser(id) {
  await api.delete(`/user/delete-user/${id}`)
  fetchUsers()
}

onMounted(fetchUsers)
</script>

<template>
  <div>
    <h2>Users</h2>
    <form @submit.prevent="createUser">
      <input v-model="newUser.username" placeholder="Username" required />
      <input v-model="newUser.email" placeholder="Email" type="email" required />
      <input v-model="newUser.password" placeholder="Password" type="password" required />
      <input v-model="newUser.role" placeholder="Role" />
      <button>Create</button>
    </form>
    <ul>
      <li v-for="u in users" :key="u.id">
        <input v-model="u.username" /> | 
        <input v-model="u.email" /> |
        <input v-model="u.role" /> |
        <button @click="updateUser(u)">Update</button>
        <button @click="deleteUser(u.id)">Delete</button>
      </li>
    </ul>
  </div>
</template>
