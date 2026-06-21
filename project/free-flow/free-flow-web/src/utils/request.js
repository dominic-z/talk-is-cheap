import axios from 'axios'


// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL, // 从环境变量获取基础URL，https://www.doubao.com/thread/w79b130c691ff2269
  timeout: 5000 // 请求超时时间
})


// 请求拦截器
request.interceptors.request.use(
  req => {
    console.log("req interceptor, path",req.url, req)
    req.headers['Access-Control-Allow-Origin'] = "*"
    return req
  },
  error => {
    // 关闭加载动画
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log("resp interceptor, path",response.request.responseURL,response)
    
    const respBody = response.data
    
    // 根据后端约定的状态码处理
    if (respBody.code !== 0) {
      return Promise.reject(new Error(respBody.msg || '请求出错'))
    } else {
      return respBody
    }
  },
  error => {
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

export default request