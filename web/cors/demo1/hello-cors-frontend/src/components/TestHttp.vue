<script setup>
import axiosUtil from '../utils/AxiosUtil';

function get(url){
    axiosUtil.get(url).then(resp => {
            console.log(resp)
        })
}

function simplePost(url) {

    axiosUtil.post(url, {
        "message": "this is a post"
    }).then(resp => {
            console.log(resp)
        })
}

function post(uri) {

    axiosUtil.post(uri, {
        "message": "this is a post"
    }, {
        // 模拟复杂请求，因为简单请求不会发送option请求
        headers: {

            'customized': 'customized'
        },
        // 跨域请求会把cookie带上
        withCredentials: true
    })
        .then(resp => {
            console.log(resp)
            console.log(resp.headers['littlemouse'])
            console.log(resp.headers['bigmouse'])
        })
        .catch(reason => {
            console.log("error")
            console.log(reason)
        })

}

</script>

<template>
    <div>
        login: <button @click="simplePost('/api/login')">login</button>
    </div>
    <div>
        logout: <button @click="simplePost('/api/logout')">logout</button>
    </div>
    <div>
        send a request to post1: <button @click="post('/api/post1')">post1</button>
    </div>
    <div>
        send a request to post2: <button @click="post('/api/post2')">post2</button>
    </div>
    <div>
        send a request to post3: <button @click="post('/api/post3')">post3</button>
    </div>


</template>