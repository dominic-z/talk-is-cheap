<script setup>
import axiosUtil from '../utils/AxiosUtil';

function post(uri){

    axiosUtil.post(uri,{
        "message":"this is a post"
    },{
        // 模拟复杂请求，因为简单请求不会发送option请求
        headers:{
            'customized':'customized'
        }
    })
    .then(resp=>{
        console.log(resp)
    })
    .catch(reason=>{
        console.log("error")
        console.log(reason)
    })

}

</script>

<template>

<div>
    send a request to post1: <button @click = "post('/api/post1')">post1</button>
</div>
<div>
    send a request to post2: <button @click = "post('/api/post2')">post2</button>
</div>
<div>
    send a request to post3: <button @click = "post('/api/post3')">post3</button>
</div>


</template>