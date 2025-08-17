<script setup>

import { useStateStore } from './StateStore.js';
import readStateStore from './ReadStateStore.js';
import WritableStateStore from './WritableStateStore.js'

const stateStore = useStateStore()

function myPatch() {
    stateStore.$patch(
        {
            count: stateStore.count + 1,
            age: 120,
            name: 'KK',
        }
    )
}


function patchIds() {
    stateStore.$patch((state)=>{
        state.ids.push(2)
    })
        
}


stateStore.$subscribe((mutation,state)=>{
    console.log('in subscribe')
    console.log(mutation)
    console.log(state)
    
})


</script>

<template>

    <div>
        stateStore

        <div>
            <button @click="stateStore.update">update</button>
            <button @click="stateStore.$reset">reset</button>
            <button @click="myPatch">myPatch</button>
            <button @click="patchIds">patchIds</button>
        </div>
        <div>
            count: {{ stateStore.count }}
        </div>
        <div>
            age: {{ stateStore.age }}
        </div>
        <div>
            name: {{ stateStore.name }}
        </div>
        <div>
            ids: {{ stateStore.ids }}
        </div>
    </div>

    <hr />

    <div>
        readStateStore
        <div>
            count: {{ readStateStore.computed.count() }}
        </div>

        <div>
            myOwnName: {{ readStateStore.computed.myOwnName() }}
        </div>

        <div>
            double: {{ readStateStore.computed.double() }}
        </div>

        <div>
            magicValue: {{ readStateStore.computed.magicValue() }}
        </div>
    </div>


    <hr />

    <div>
        WritableStateStore
        <div>
            <button @click="() => WritableStateStore.computed.count.set(2)">update count</button>
            <button @click="() => WritableStateStore.computed.myOwnName.set(3)">update myOwnName</button>

        </div>

        <div>
            count: {{ WritableStateStore.computed.count.get() }}
        </div>

        <div>
            myOwnName: {{ readStateStore.computed.myOwnName() }}
        </div>

    </div>

</template>