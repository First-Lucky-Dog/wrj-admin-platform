// 引入unocss css
import '@/plugins/unocss'

// 导入全局的svg图标
import '@/plugins/svgIcon'

// 初始化多语言
import { setupI18n } from '@/plugins/vueI18n'

// 引入状态管理
import { setupStore } from '@/store'

// 全局组件
import { setupGlobCom } from '@/components'

// 引入 element-plus
import { setupElementPlus } from '@/plugins/elementPlus'

// 引入 form-create
import { setupFormCreate } from '@/plugins/formCreate'

// 引入全局样式
import '@/styles/index.scss'

// 引入动画
import '@/plugins/animate.css'

// 路由
import router, { setupRouter } from '@/router'

// 指令
import { setupAuth, setupMountedFocus } from '@/directives'

import { createApp } from 'vue'

import App from './App.vue'

import './permission'

import '@/plugins/tongji' // 百度统计
import Logger from '@/utils/Logger'

import VueDOMPurifyHTML from 'vue-dompurify-html' // 解决v-html 的安全隐患

// wangEditor 插件注册
import { setupWangEditorPlugin } from '@/views/bpm/model/form/PrintTemplate'

import print from 'vue3-print-nb' // 打印插件

const basePath = import.meta.env.BASE_URL || '/'
;(window as any).CESIUM_BASE_URL = `${basePath.endsWith('/') ? basePath : `${basePath}/`}cesium/`
const enableTileSwCache =
  String(import.meta.env.VITE_ENABLE_TILE_SW_CACHE ?? 'true').toLowerCase() !== 'false'

const registerTileCacheServiceWorker = () => {
  if (!enableTileSwCache || !('serviceWorker' in navigator)) {
    return
  }
  const swBasePath = basePath.endsWith('/') ? basePath : `${basePath}/`
  const swUrl = `${swBasePath}sw.js`
  window.addEventListener('load', () => {
    navigator.serviceWorker.register(swUrl).catch((error) => {
      console.warn('[SW] 天地图瓦片缓存 Service Worker 注册失败:', error)
    })
  })
}

// 创建实例
const setupAll = async () => {
  const app = createApp(App)

  await setupI18n(app)

  setupStore(app)

  setupGlobCom(app)

  setupElementPlus(app)

  setupFormCreate(app)

  setupRouter(app)

  // directives 指令
  setupAuth(app)
  setupMountedFocus(app)

  // wangEditor 插件注册
  setupWangEditorPlugin()

  await router.isReady()

  app.use(VueDOMPurifyHTML)

  // 打印
  app.use(print)

  app.mount('#app')
  registerTileCacheServiceWorker()
}

setupAll()

Logger.prettyPrimary(`欢迎使用`, import.meta.env.VITE_APP_TITLE)
