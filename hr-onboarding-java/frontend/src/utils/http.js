/**
 * axios 实例与 token 存取。
 *
 * 约定与后端一致：响应体 { code, message, data, errors? }，code===0 为成功。
 * 401 统一清理本地会话并跳转登录页，避免各页面重复处理。
 */
import axios from 'axios'

const TOKEN_KEY = 'hr_token'
const USER_KEY = 'hr_user'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：附带 JWT
http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/** 避免重复跳转登录页 */
let redirecting = false

/** 这些接口本身就会用 401 表达业务失败（如账号密码错误），不能当作会话失效处理 */
const AUTH_EXEMPT_PATHS = ['/auth/login']

http.interceptors.response.use(
  (response) => {
    // 业务错误（HTTP 200 但 code !== 0）在此抛出，保持调用方写法统一
    const body = response.data
    if (body && typeof body.code !== 'undefined' && body.code !== 0) {
      return Promise.reject(body)
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data
    const url = error.config?.url || ''

    // 登录接口的 401 表示账号密码错误，交由调用方展示提示，不能跳登录页
    const isAuthExempt = AUTH_EXEMPT_PATHS.some((path) => url.startsWith(path))

    if (status === 401 && !isAuthExempt && !redirecting) {
      redirecting = true
      auth.clear()
      window.location.replace('/login')
      return Promise.reject(body || { message: '登录已失效' })
    }
    return Promise.reject(body || { message: error.message || '网络异常' })
  }
)

export const auth = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
  },
  getUser() {
    return localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY) || ''
  },
  /** 记住我写入 localStorage，否则写入 sessionStorage（关标签页即失效） */
  save(token, user, remember) {
    this.clear()
    const target = remember ? localStorage : sessionStorage
    target.setItem(TOKEN_KEY, token)
    target.setItem(USER_KEY, user)
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    sessionStorage.removeItem(USER_KEY)
  },
  async logout() {
    try {
      await http.post('/auth/logout')
    } catch {
      /* JWT 无状态，登出失败不阻塞本地清理 */
    }
    this.clear()
  }
}

export default http