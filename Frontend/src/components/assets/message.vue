<template>
    <!-- zwraca powiadomienie w w odpowiednim stylu w zaleznosci od informacji -->
    <article v-if="display == true" v-bind:class="['notification', 'is-light', type]">
        <button class="delete" aria-label="delete" v-on:click="display = false"></button>
        <span v-html="body">
        </span>
    </article>
</template>

<script>
    export default {
        name: "message",
        data() {
            return {
                display: false,
                header: "",
                body: "",
                type: ""
            }
        },
        //zmiana typu (a tym samym stylu) wiadomosci w zaleznosci od metody
        methods: {
            showSuccess: function(header, body){
                this.type = 'is-success'
                this.header = header
                this.body = body
                this.display = true
            },

            showDanger: function(header, body){
                this.type = 'is-danger'
                this.header = header
                this.body = body
                this.display = true
            }
        }
    }
</script>

<style scoped>

</style>