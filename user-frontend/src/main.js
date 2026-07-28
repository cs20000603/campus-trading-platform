import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import '@/assets/css/global.css'

const app = createApp(App)

app.use(router)
app.use(ElementPlus, {
    locale: zhCn,
})
app.mount('#app')

// WebSocket 连接（需要用户 ID）
const user = localStorage.getItem('system-user')
if (user) {
  try {
    const userData = JSON.parse(user)
    import('@/utils/websocket').then(m => m.connect(userData.id)).catch(() => {})
  } catch (e) { /* ignore */ }
}

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}