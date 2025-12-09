import Vue from 'nativescript-vue';

import Login from './components/Login.vue';
import Home from './components/Home.vue';

import { getAccessToken } from './services/core/auth-store';

new Vue({
    render(h) {
        const token = getAccessToken();

        return h(
            'frame',
            [ h(token ? Home : Login) ]  // If token exists → Home, else Login
        );
    }
}).$start();
