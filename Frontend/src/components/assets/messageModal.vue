<template>
    <div class="modal" v-bind:class="{ 'is-active': display }">
        <div class="modal-background"></div>
        <!-- zwraca powiadomienie w w odpowiednim stylu w zaleznosci od informacji -->
        <div class="modal-content">
            <article v-bind:class="['notification', 'is-light', type]">
                <button class="delete" aria-label="delete" v-on:click="display = false"></button>
                <span v-html="body">
                </span>
            </article>
        </div>
    </div>
</template>


<script>
    export default {
        name: "message-modal",
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