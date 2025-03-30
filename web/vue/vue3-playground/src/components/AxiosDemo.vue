<script setup>
import axios from 'axios';



async function testGet() {
    // 请求超过30秒则判定为超时
    const instance = axios.create({
        baseURL: '/httpbin-api',
        timeout: 30000,
        // 跨域请求会将cookie带上
        withCredentials: true,
    });

    let res = await instance.get("/get",{params:{'a':1}})
    console.log(res)
}

function interceptorPost() {
    // 普通的get请求
    // instance.get(url,config)
    const instance = axios.create({
        baseURL: '/local',
        timeout: 30000,
        withCredentials: true,
    });

    instance.interceptors.request.use(
        config => {
            // 在发送请求之前做些什么，例如添加请求头，添加请求体
            console.log('过滤器中打印config',config)
            config.headers['Authorization'] = 'Bearer your_token';
            config.headers['Haha'] = 'Haha1';
            config.data['interceptor']='myinterceptors'
            return config;
        },
        error => {
            // 对请求错误做些什么
            // 可以把后端服务关了测试
            console.error('请求出错:', error);
            return Promise.reject(error);
        }
    );

    // 添加响应拦截器
    instance.interceptors.response.use(
        response => {
            // 对响应数据做点什么，例如提取数据
            console.log('响应:',response)
            return response.data;
        },
        error => {
            // 对响应错误做点什么，例如根据状态码处理错误
            // 可以把后端服务关了测试
            console.error('响应出错:', error);
            if (error.response) {
                switch (error.response.status) {
                    case 401:
                        console.log('未授权，请登录');
                        break;
                    case 404:
                        console.log('请求资源不存在');
                        break;
                    case 500:
                        console.log('服务器内部错误');
                        break;
                    default:
                        console.log('未知错误');
                }
            }
            // 替换原本的error
            return Promise.reject('hahah');
            // return Promise.reject(error);
        }
    );

    instance.post('/postHello', {})
    .catch(err=>{
        console.log('被替换的error')
        console.error(err)
    })
        

}

</script>

<template>


    <button @click="testGet">实际访问的是httpbin.org/get</button>
    <button @click="interceptorPost">实际访问的是locahost:8082/postHello</button>
</template>