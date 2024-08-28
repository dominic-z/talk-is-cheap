<script setup>
import { ref, reactive, computed } from 'vue';
let text1 = ref("")
let text2 = ref("")

function setText1(t) {
    text1.value = t
    console.log(t)
    console.log(text1.value)
}

let checked = ref(true)

let checkNames = ref([])
let picked = ref("one")
let selected = ref("")
const options = ref([
    { text: 'One', value: 'A' },
    { text: 'Two', value: 'B' },
    { text: 'Three', value: 'C' }
])
let multipleselected = ref([])
let toggle = ref("yes")

let pick = ref(true)
let first = ref('first')
let second = ref('second')
let dynamicselected = ref("")
let msg = ref("")
</script>

<template>
    <!-- 将input的文本与text1绑定，相当于input框内的文本一直就是text1，然后使用事件更新text1来更新文本框的内容 -->
    <input :value="text1" @input="event => setText1(event.target.value)">

    <input v-model="text2" placeholder="edit me">

    <input type="checkbox" id="checkbox" v-model="checked" />
    <label for="checkbox">{{ checked }}</label>

    <!-- checkebox监听列表的话，会将value丢进checknames里 -->
    <div>Checked names: {{ checkNames }}</div>
    <input type="checkbox" id="jack" value="jack" v-model="checkNames">
    <label for="jack">Jack</label>
    <input type="checkbox" id="john" value="John" v-model="checkNames">
    <label for="john">John</label>
    <!-- 这种方式似乎不行，需要点击两次标签才会选上，第一次会像checkNames里添加一个on，第二次点击才会选中 -->
    <input type="checkbox" id="noValue" v-model="checkNames">
    <label for="noValue">noValue</label>



    <!-- 单选框的v-model工作模式，picked如果和单选框的value相同，就说明是选中的，比如初始值设为one，则初始状态下两个radio都是没有选中的 -->
    <div>Picked: {{ picked }}</div>
    <input type="radio" id="one" value="One" v-model="picked" />
    <label for="one">One</label>
    <input type="radio" id="two" value="Two" v-model="picked" />
    <label for="two">Two</label>


    <div>Selected: {{ selected }}</div>


    <!-- <select> 会绑定 value property 并侦听 change 事件。 -->
    <select v-model="selected">
        <option disabled value="">Please select one</option>

        <option v-for="option in options" :value="option.value">
            {{ option.text }}
        </option>
    </select>

    <!-- ctrl多选 -->
    <div>Selected: {{ multipleselected }}</div>
    <select v-model="multipleselected" multiple>
        <option>A</option>
        <option>B</option>
        <option>C</option>
    </select>

    <!-- checkbox是选中的时候，将true-value赋值给toggle -->
    <div>
        <input id="dynamicCheckbox" type="checkbox" v-model="toggle" true-value="yes" false-value="no" />
        <label for="dynamicCheckbox">{{ toggle }}</label>
    </div>


    <div>
        <input type="radio" v-model="pick" :value="first" />
        <input type="radio" v-model="pick" :value="second" />
        {{ pick }}
    </div>


    <div>
        <select v-model="dynamicselected">
            <!-- 内联对象字面量 -->
            <option :value="{ number: 123 }">123</option>
        </select>
        {{ dynamicselected.number }}
    </div>

    <div>
        <!-- 在 "change" 事件后同步更新而不是 "input" -->
        <!-- 具体来说，就是input从选中状态移除的时候触发，因为默认v-model监听的是input事件，而不是change事件-->
        <input v-model.lazy="msg" />
        {{ msg }}
    </div>

</template>