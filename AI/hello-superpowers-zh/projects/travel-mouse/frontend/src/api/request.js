import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code !== 200) {
      alert(message || '请求失败')
      return Promise.reject(new Error(message))
    }
    return data
  },
  error => {
    alert(error.response?.data?.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
