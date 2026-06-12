import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import './styles/global.css'

const app = createApp(App)

// 全局错误处理器 — 防止组件渲染错误导致整个应用崩溃
app.config.errorHandler = (err, instance, info) => {
  console.error('[全局错误]', err)
  console.error('组件:', instance?.$options?.name || instance)
  console.error('位置:', info)
}

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
