<template>
    <!-- komponent budujacy podglad danego paragonu  -->
    <div class="box content">

        <message ref="message"></message>
        <h1 class="title has-text-grey-darker">Rachunek</h1>
        <p>
            Zarządzanie rachunkiem
        </p>

        <div class="form">
            <div class="field">
                <label class="label">Kwota</label>
                <div class="control has-icons-left">
                    <input v-model="billDataModel.price" type="text" placeholder="Kwota" class="input" required>
                    <span class="icon is-small is-left">
                        <i class="fa fa-envelope"></i>
                    </span>
                </div>
            </div>

            <div class="field">
                <label class="label">Data</label>
                <div class="control has-icons-left">
                    <input v-model="billDataModel.date" type="datetime-local" placeholder="Data" class="input" required>
                </div>
            </div>

            <div class="field">
                <label class="label">Opis</label>
                <div class="control has-icons-left">
                    <textarea v-model="billDataModel.desc" class="textarea" required></textarea>
                </div>
            </div>

            <div class="field">
                <label class="label">Zdjęcie</label>
                <img v-bind:src="billDataModel.photo">
                <figure v-if="billDataModel.photo" class="figure">

                </figure>
                <span v-else>Zdjęcie nie zostało załączone</span>
                <div class="control has-icons-left">
                    <input type="file" id="file_input" v-on:change="encodeImageFileAsURL()" />
                </div>
            </div>


            <div class="field is-grouped">
                <div class="control">
                    <button v-on:click="save()" v-on:keyup.enter="save()" class="button is-success">
                        Aktualizuj
                    </button>
                </div>
                <div class="control">
                    <router-link to="/" class="button is-link">
                        Powrót
                    </router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

    import axios from 'axios'
    import Message from "../assets/message.vue";

    export default {
        name: "bill",

        components: {Message},
        data(){
            return {
                billDataModel: {},
                billData: {}
            }
        },

        beforeMount() {
            this.load()
        },

        methods: {
            encodeImageFileAsURL: function(){
                let self = this
                let element = document.getElementById("file_input")
                let file = element.files[0]
                let reader = new FileReader()
                reader.onloadend = function() {

                    self.$set(self.billDataModel, "photo", reader.result)

                }
                reader.readAsDataURL(file)
            },

            //zapisanie zmian w charakterystyce paragonu
            save: function() {
                let self = this
                let error = ""
                let keys = Object.keys(self.billDataModel);
                const saveNext = function(index, callback) {

                    console.log("saveNext", index)
                    if(index >= keys.length) {
                        callback()
                        return
                    }
                    else{
                        if(self.billDataModel[keys[index]] != self.billData[keys[index]]) {
                            self.saveOne(keys[index], self.billDataModel[keys[index]]).
                            then(m => {
                                saveNext(index+1, callback)
                            }).
                            catch(e => {
                                error += "Blad przy modyfikacji " + keys[index] + "parametru: " + e + "<br>"
                                index ++
                                saveNext(index+1,callback)
                            })
                        }else{
                            saveNext(index+1,callback)
                        }
                    }
                }

                saveNext(0, function() {
                    if(error != ""){
                        self.$refs.message.showDanger("", error)
                    }else{
                        self.$refs.message.showSuccess("", "Poprawnie zmodyfikowano")
                    }
                })
            },

            //metoda zapisuje konkretny parametr do konta (wysylana jest kilka razy z roznymi parametrami)
            saveOne: function(paramName, paramValue){
                return new Promise((resolve, reject) => {

                    let token = window.sessionStorage.getItem("token")
                    console.log("modyfied params", paramName, paramValue)
                    axios.post('/modifyBill', {
                        token: token,
                        id: this.$route.params.id,
                        param: paramName,
                        value: paramValue
                    }).then(m => {
                        if(m.data.error){
                            reject(m.data.error)
                        }else{
                            resolve(paramName)
                        }
                    }).catch(e => {
                        console.error(e)
                        this.$refs.message.showDanger("", e)
                    })
                })
            },
            //aktualizacja daty
            changeDate(d){
                d = new Date(d)
                d = d.toLocaleDateString().split(".").reverse().map((s, i, a) => {
                    if(s.length < 2) return '0' + s;
                    else return s
                }).join("-") + 'T' + d.getHours().toString().padStart(2, '0') + ":" + d.getMinutes().toString().padStart(2, '0')
                return d
            },

            // metoda pozwala na pobranie rachunku z serwera i wyswietleniu otrzymanych danych na ekranie
            load: function(){
                let token = window.sessionStorage.getItem("token")
                axios.post('/getBill', {
                    token: token,
                    id: this.$route.params.id
                }).then(m => {
                    if(m.data.error){
                        this.$refs.message.showDanger("", "Odmowa dostepu do rachunku")
                    }else{
                        this.billData = JSON.parse(JSON.stringify(m.data))
                        this.billDataModel = JSON.parse(JSON.stringify(m.data))
                        this.billData.date = this.changeDate(this.billData.date)
                        this.billDataModel.date = this.changeDate(this.billDataModel.date)
                    }

                }).catch(e => {
                    console.error(e)
                    this.$refs.message.showDanger("", e)
                })
            }
        }
    }
</script>

<style scoped>

</style>