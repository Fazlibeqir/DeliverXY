import Vue from 'nativescript-vue';
import AppEntry from './components/AppEntry.vue';

new Vue({
    render: h => h('frame', [h(AppEntry)])
}).$start();
