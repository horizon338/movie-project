import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, me as meApi } from '@/api/auth'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUser(),
  }),

  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN',
    displayName: (state) => state.user?.nickname || state.user?.username || '',
  },

  actions: {
    async login(form) {
      const res = await loginApi(form)
      this.applyAuth(res.data)
      return res.data
    },

    async register(form) {
      const res = await registerApi(form)
      this.applyAuth(res.data)
      return res.data
    },

    applyAuth({ token, user }) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },

    /** 刷新页面后用 token 拉取最新用户信息 */
    async restore() {
      if (!this.token) return
      try {
        const res = await meApi()
        this.user = res.data
        localStorage.setItem(USER_KEY, JSON.stringify(res.data))
      } catch {
        this.logout()
      }
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
  },
})
