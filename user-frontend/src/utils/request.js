// 封装Axios实例：统一配置、请求拦截（自动附加token）、响应拦截（解析数据）
import axios from 'axios'

// 创建Axios实例
const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,  // 后端接口基础地址
    timeout: 5000  // 请求超时时间5秒
})

// 请求拦截器：设置请求头，自动从localStorage附加token
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8'
    const user = localStorage.getItem('system-user')
    if (user) {
        const parsed = JSON.parse(user)
        if (parsed && parsed.token) {
            config.headers['token'] = parsed.token  // 将token写入请求头
        }
    }
    return config
}, error => {
    return Promise.reject(error)
})

// 响应拦截器：自动解析字符串类型的响应数据
request.interceptors.response.use(response => {
    let res = response.data
    if (typeof res === 'string') {
        res = JSON.parse(res)  // 字符串转JSON对象
    }
    return res
}, error => {
    console.log('err' + error)  // 打印错误信息
    return Promise.reject(error)
})

export default request