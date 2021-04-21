<template>
    <div class="has-background-image">
        <img class="background-image" src="../../images/bg.jpg">
        <section id="panel-login" class="hero has-background-brand-yellow is-fullheight">
            <div class="hero-body">
                <div class="container">
                    <div class="columns is-centered">
                        <div class="column is-12-tablet is-9-desktop is-8-widescreen">
                            <router-view></router-view>
                            <button class="button is-link" v-on:click="logout()">Wyloguj</button>
                            <button class="button is-danger" v-on:click="remove()">Usun konto</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>
</template>

<script>
    import axios from 'axios'

    export default {
        name: "router",
        data() {
            return {
                token: null
            }
        },

        mounted() {
            //weryfikacja zalogowania uzytkownika w momencie zaladowania strony
            if(this.token == null){
                let t = window.sessionStorage.getItem("token")
                console.log(t)
                if(t!=null){
                    this.verify(t)
                    this.token = t
                }else{
                    this.$router.push("/login").catch(err => {})
                }
            }
        },

        methods: {
            //ponowna weryfikacja tokenu w zaleznosci od checi wykonania akcji
            verify: function (token) {
                axios.post("/verify",{
                    token: token
                }).then(m => {
                    if(m.data.verify != 'ok'){
                        window.sessionStorage.setItem("token", null)
                        this.$router.push({path: "/sessionend", query: {d: this.$router.currentRoute.path}}).catch(err => {})
                    }
                }).catch(e => {
                    window.sessionStorage.setItem("token", null)
                    this.$router.push({path: "/sessionend", query: {d: this.$router.currentRoute.path}}).catch(err => {})
                })
            },

            //metoda wylogowuje uzytkownika
            logout: function () {

                axios.post("/logout",{
                    token: this.token
                }).then(m => {
                    window.sessionStorage.setItem("token", null)
                    this.$router.push({path: "/login"}).catch(err => {})
                }).catch(e => {
                    console.error(e)
                    window.sessionStorage.setItem("token", null)
                    this.$router.push({path: "/login"}).catch(err => {})
                })
            },

            // metoda usuwa konto uzytkownika 
            remove: function () {
                axios.post("/deleteUser",{
                    token: this.token
                }).then(m => {
                    window.sessionStorage.setItem("token", null)
                    this.$router.push({path: "/login"}).catch(err => {})
                }).catch(e => {
                    console.error(e)
                    window.sessionStorage.setItem("token", null)
                    this.$router.push({path: "/login"}).catch(err => {})
                })
            }
        }
    }
</script>

<style scoped>

</style>