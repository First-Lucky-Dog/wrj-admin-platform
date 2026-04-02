<template>
  <div class="uav-screen" :class="{ 'is-global-view': mapMode === 'global' }">
    <!-- 顶部标题栏 -->
    <header class="screen-header">
      <!-- 左侧装饰线 -->
      <div class="header-deco header-deco-left"></div>

      <div class="header-left">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2L4.5 20.29l.71.71L12 18l6.79 3 .71-.71z" />
          </svg>
        </div>
        <div class="header-title-wrapper">
          <h1 class="header-title">无人机低空管理平台</h1>
          <span class="header-subtitle">UAV Command & Control Center</span>
        </div>
      </div>

      <div class="header-center">
        <div class="header-tabs">
          <template v-if="headerMenus.length > 0">
            <div class="tab-item" :class="{ active: isHeaderMenuActive(menu) }" v-for="menu in headerMenus"
              :key="menu.matchPath" :title="menu.title" @click="handleHeaderMenuClick(menu)">
              <Icon :icon="menu.icon || 'mdi:view-dashboard-outline'" :size="18" />
              <span class="tab-text">{{ menu.title }}</span>
            </div>
          </template>
          <div v-else class="tab-item active">
            <Icon icon="mdi:view-dashboard-outline" :size="18" />
            <span class="tab-text">系统菜单加载中</span>
          </div>
        </div>
      </div>

      <div class="header-right">
        <div class="header-status-group">
          <div class="header-item weather-item">
            <Icon icon="mdi:weather-partly-cloudy" :size="18" />
            <span class="item-text">晴 25°C</span>
          </div>
          <div class="header-item time-item">
            <Icon icon="mdi:clock-outline" :size="18" />
            <span class="item-text">{{ currentTime }}</span>
          </div>
          <div class="header-item mode-item anchor-item"
            :class="{ active: isCenterAnchorSwitchEnabled, disabled: !isCenterAnchorSwitchEnabled }"
            :aria-disabled="!isCenterAnchorSwitchEnabled" @click="handleCenterAnchorSwitch">
            <Icon icon="mdi:crosshairs-gps" :size="18" />
            <span class="item-text">{{ isCenterAnchorSwitchEnabled ? '回中心锚点' : '中心锚点' }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧装饰线 -->
      <div class="header-deco header-deco-right"></div>
    </header>

    <!-- 主内容区 -->
    <main class="screen-main">
      <!-- Cesium 地图背景 -->
      <CesiumMap ref="cesiumMapRef" class="map-layer" :drones="drones" @zoom-mode-change="handleZoomModeChange"
        @camera-mode-change="handleCameraModeChange" />

        <!-- 左侧面板 -->
      <div class="left-panels">
        <ProvinceDataCard class="province-data-card" :stats="droneStats" />
        <LeftPanel class="left-panel" :class="{ 'is-collapsed': isLeftPanelCollapsed }"
          :collapsed="isLeftPanelCollapsed" :drones="drones" :selected-drone-id="selectedDroneId"
          :show-illegal-only="showIllegalOnly" :active-categories="activeCategories"
          @toggle-collapse="handleLeftPanelCollapse" @select-drone="handleDroneSelect"
          @filter-category="handleCategoryFilter" @filter-illegal="handleIllegalFilter" />
      </div>

      <!-- 右侧面板 -->
       <div class="right-panels">
         <RightPanel class="right-panel" :class="{ 'is-collapsed': isRightPanelCollapsed }"
           :collapsed="isRightPanelCollapsed" :missions="missions" :mission-stats="missionStats"
           @toggle-collapse="handleRightPanelCollapse" />
       </div>
    </main>


  </div>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@/components/Icon'
import { usePermissionStore } from '@/store/modules/permission'
import { pathResolve } from '@/utils/routerHelper'
import { isUrl } from '@/utils/is'
import CesiumMap from './components/CesiumMap.vue'
import LeftPanel from './components/LeftPanel.vue'
import ProvinceDataCard from './components/ProvinceDataCard.vue'
import RightPanel from './components/RightPanel.vue'
import { useHomeDemoState } from './demo/useHomeDemoState'
import { ALL_DRONE_CATEGORIES, type DroneCategory } from './demo/mock-data'

defineOptions({ name: 'UavScreen' })

type ZoomMode = 'near' | 'global'
type CameraInteractionMode = 'overview' | 'drone-focus'

interface ZoomModeChangePayload {
  mode: ZoomMode
  height: number
  source: 'auto' | 'manual'
}

interface DroneSelectPayload {
  id: number
  name: string
}

interface CesiumMapExpose {
  focusDroneById: (droneId: number) => void
  setFilterCategories: (categories: DroneCategory[]) => void
  setShowIllegalOnly: (value: boolean) => void
  switchToCenterAnchor: () => void
  getCameraInteractionMode: () => CameraInteractionMode
}

interface HeaderMenuItem {
  title: string
  icon: string
  path: string
  matchPath: string
}

// 当前时间
const currentTime = ref('')
// 地图模式
const mapMode = ref<ZoomMode>('near')
// 轨道相机锚点模式（中心 / 无人机）
const cameraInteractionMode = ref<CameraInteractionMode>('overview')
const showIllegalOnly = ref(false)
const activeCategories = ref<DroneCategory[]>([...ALL_DRONE_CATEGORIES])
const selectedDroneId = ref<number | null>(null)
const cesiumMapRef = ref<CesiumMapExpose | null>(null)
const isLeftPanelCollapsed = ref(false)
const isRightPanelCollapsed = ref(false)
const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const { drones, missions, droneStats, missionStats } = useHomeDemoState()

const getFullPath = (parentPath: string, path: string) => {
  return isUrl(path) ? path : pathResolve(parentPath, path)
}

const getFirstVisibleLeafPath = (record: AppRouteRecordRaw, parentPath: string): string => {
  const visibleChildren = (record.children || []).filter((child) => !child.meta?.hidden)
  if (visibleChildren.length === 0) {
    return parentPath
  }
  const firstChild = visibleChildren[0]
  const childPath = getFullPath(parentPath, String(firstChild.path || ''))
  if (!isUrl(childPath) && firstChild.children && firstChild.children.length > 0) {
    return getFirstVisibleLeafPath(firstChild, childPath)
  }
  return childPath
}

const toMenuItem = (record: AppRouteRecordRaw): HeaderMenuItem | null => {
  if (record.meta?.hidden) {
    return null
  }
  const rawPath = String(record.path || '')
  if (!rawPath || rawPath === '/') {
    return null
  }
  const matchPath = getFullPath('/', rawPath)
  const title = String(record.meta?.title || record.name || '').trim()
  if (!title) {
    return null
  }

  let targetPath = matchPath
  if (!isUrl(matchPath)) {
    if (typeof record.redirect === 'string' && record.redirect && record.redirect !== 'noredirect') {
      targetPath = record.redirect.startsWith('/')
        ? record.redirect
        : getFullPath(matchPath, record.redirect)
    } else {
      targetPath = getFirstVisibleLeafPath(record, matchPath)
    }
  }

  return {
    title,
    icon: String(record.meta?.icon || 'mdi:view-dashboard-outline'),
    path: targetPath,
    matchPath
  }
}

const headerMenus = computed(() => {
  return permissionStore.getRouters
    .map((routeRecord) => toMenuItem(routeRecord))
    .filter((item): item is HeaderMenuItem => !!item)
})

const activeMenuPath = computed(() => {
  const fromMeta = route.meta?.activeMenu
  if (typeof fromMeta === 'string' && fromMeta) {
    return fromMeta
  }
  return route.path
})

const isHeaderMenuActive = (menu: HeaderMenuItem) => {
  if (isUrl(menu.matchPath)) {
    return false
  }
  return (
    activeMenuPath.value === menu.matchPath ||
    activeMenuPath.value.startsWith(`${menu.matchPath}/`)
  )
}

const handleHeaderMenuClick = async (menu: HeaderMenuItem) => {
  if (isUrl(menu.path)) {
    window.open(menu.path)
    return
  }
  if (route.path === menu.path) {
    return
  }
  await router.push(menu.path)
}

const isCenterAnchorSwitchEnabled = computed(() => cameraInteractionMode.value === 'drone-focus')

const handleCenterAnchorSwitch = () => {
  if (!isCenterAnchorSwitchEnabled.value) return
  cesiumMapRef.value?.switchToCenterAnchor()
}

const handleZoomModeChange = (payload: ZoomModeChangePayload) => {
  mapMode.value = payload.mode
}

const handleCameraModeChange = (mode: CameraInteractionMode) => {
  cameraInteractionMode.value = mode
}

const handleDroneSelect = (payload: DroneSelectPayload) => {
  selectedDroneId.value = payload.id
  cesiumMapRef.value?.focusDroneById(payload.id)
}

const handleCategoryFilter = (categories: DroneCategory[]) => {
  activeCategories.value = categories
  cesiumMapRef.value?.setFilterCategories(categories)
}

const handleIllegalFilter = (isIllegalOnly: boolean) => {
  showIllegalOnly.value = isIllegalOnly
  cesiumMapRef.value?.setShowIllegalOnly(isIllegalOnly)
}

const handleLeftPanelCollapse = (collapsed: boolean) => {
  isLeftPanelCollapsed.value = collapsed
}

const handleRightPanelCollapse = (collapsed: boolean) => {
  isRightPanelCollapsed.value = collapsed
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

let timer: number | null = null

onMounted(() => {
  updateTime()
  timer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.uav-screen {
  --uav-bg: #0a0e27;
  --uav-card-bg: rgba(16, 24, 48, 0.85);
  --uav-primary: #00d4ff;
  --uav-success: #00ff88;
  --uav-warning: #ffaa00;
  --uav-danger: #ff4d4f;
  --uav-border: rgba(0, 212, 255, 0.3);
  --uav-text-primary: #ffffff;
  --uav-text-secondary: rgba(255, 255, 255, 0.65);
  --uav-shadow: 0 0 20px rgba(0, 212, 255, 0.2);
  --uav-shadow-hover: 0 0 30px rgba(0, 212, 255, 0.4);

  /* Fully Transparent Glass Effect System */
  --uav-panel-bg-opacity: 0.02;
  --uav-panel-bg: transparent;
  --uav-panel-blur: blur(15px);
  --uav-mask-opacity-center: 0.01;
  --uav-mask-opacity-edge: 0.15;

  /* Enhanced Border Visibility */
  --uav-border-opacity: 0.7;
  --uav-border-glow: 0 0 15px rgba(0, 212, 255, 0.4);

  /* Text Readability Enhancement */
  --uav-text-shadow: 0 0 8px rgba(0, 0, 0, 0.8), 0 2px 4px rgba(0, 0, 0, 0.6);
  --uav-text-shadow-strong: 0 0 12px rgba(0, 0, 0, 0.9), 0 2px 6px rgba(0, 0, 0, 0.7);

  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--uav-bg);
  overflow: hidden;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
}

/* 顶部标题栏 */
.screen-header {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: clamp(3.8rem, 7vh, 5rem);
  display: grid;
  grid-template-columns: minmax(16rem, 24vw) minmax(0, 1fr) auto;
  align-items: center;
  column-gap: clamp(0.5rem, 0.8vw, 1rem);
  padding: 0 clamp(0.75rem, 1.2vw, 1.5rem);
  z-index: 200;
}

.header-deco {
  position: absolute;
  top: 0;
  bottom: 0;
  width: clamp(2.5rem, 4.2vw, 4.6rem);
  pointer-events: none;
}

.header-deco-left {
  left: 0;
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.3) 0%, transparent 100%);
  clip-path: polygon(0 0, 100% 0, 76% 100%, 0 100%);
}

.header-deco-right {
  right: 0;
  background: linear-gradient(270deg, rgba(0, 212, 255, 0.3) 0%, transparent 100%);
  clip-path: polygon(24% 0, 100% 0, 100% 100%, 0 100%);
}

/* 左侧区域 */
.header-left {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: clamp(0.5rem, 0.85vw, 1rem);
  z-index: 1;
}

.header-icon {
  width: clamp(2.2rem, 2.6vw, 3rem);
  height: clamp(2.2rem, 2.6vw, 3rem);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 212, 255, 0.08);
  border: 0.08rem solid rgba(0, 212, 255, 0.62);
  border-radius: clamp(0.35rem, 0.65vh, 0.6rem);
  color: var(--uav-primary);
  box-shadow: 0 0 0.9rem rgba(0, 212, 255, 0.22);
}

.header-icon svg {
  width: clamp(1.2rem, 1.5vw, 1.65rem);
  height: clamp(1.2rem, 1.5vw, 1.65rem);
}

.header-title-wrapper {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: clamp(0.12rem, 0.28vh, 0.25rem);
}

.header-title {
  margin: 0;
  line-height: 1.08;
  font-size: clamp(1.05rem, 1.45vw, 1.6rem);
  font-weight: 600;
  background: linear-gradient(90deg, #ffffff 0%, var(--uav-primary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: clamp(0.03rem, 0.2vw, 0.14rem);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-subtitle {
  line-height: 1;
  font-size: clamp(0.62rem, 0.78vw, 0.86rem);
  color: rgba(255, 255, 255, 0.58);
  letter-spacing: clamp(0.06rem, 0.24vw, 0.18rem);
  text-transform: uppercase;
  text-shadow: var(--uav-text-shadow);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 中央 Tab 区域 */
.header-center {
  min-width: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1;
}

.header-tabs {
  width: 100%;
  max-width: min(58vw, 64rem);
  display: flex;
  align-items: center;
  gap: clamp(0.5rem, 0.8vw, 0.9rem);
  overflow-x: auto;
  overflow-y: hidden;
  padding: clamp(0.32rem, 0.6vh, 0.55rem) clamp(0.38rem, 0.8vw, 0.75rem);
  border-radius: clamp(0.42rem, 0.85vh, 0.75rem);
}

.header-tabs::-webkit-scrollbar {
  height: 0.2rem;
}

.header-tabs::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.26);
  border-radius: 0.12rem;
}

.header-tabs::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.45);
  border-radius: 0.12rem;
}

.tab-item {
  min-height: clamp(2rem, 3.2vh, 2.55rem);
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  gap: clamp(0.35rem, 0.5vw, 0.55rem);
  padding: 0 clamp(0.75rem, 1.2vw, 1.25rem);
  background: transparent;
  border: 0.06rem solid transparent;
  border-radius: clamp(0.35rem, 0.7vh, 0.65rem);
  color: var(--uav-text-secondary);
  cursor: pointer;
  transition:
    background-color 0.24s ease,
    border-color 0.24s ease,
    color 0.24s ease,
    box-shadow 0.24s ease;
  font-size: clamp(0.8rem, 0.9vw, 0.94rem);
}

.tab-item:hover {
  background: rgba(0, 212, 255, 0.12);
  border-color: rgba(0, 212, 255, 0.28);
  color: var(--uav-primary);
}

.tab-item.active {
  background: rgba(0, 212, 255, 0.17);
  border-color: rgba(0, 212, 255, 0.6);
  color: var(--uav-primary);
  box-shadow: 0 0 0.75rem rgba(0, 212, 255, 0.24);
}

.tab-text {
  font-weight: 500;
  max-width: clamp(5.2rem, 7.2vw, 7.2rem);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: var(--uav-text-shadow);
}

/* 右侧区域 */
.header-right {
  min-width: max-content;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  z-index: 1;
}

.header-status-group {
  display: flex;
  align-items: center;
  gap: clamp(0.38rem, 0.72vw, 0.8rem);
  padding: clamp(0.18rem, 0.45vh, 0.34rem);
}

.header-item {
  min-height: clamp(2rem, 3.4vh, 2.7rem);
  display: flex;
  align-items: center;
  gap: clamp(0.3rem, 0.48vw, 0.5rem);
  padding: 0 clamp(0.62rem, 0.95vw, 1rem);
  background: rgba(0, 212, 255, 0.06);
  border: 0.06rem solid rgba(0, 212, 255, 0.28);
  border-radius: clamp(0.36rem, 0.72vh, 0.65rem);
  color: var(--uav-text-secondary);
  font-size: clamp(0.78rem, 0.86vw, 0.92rem);
  line-height: 1;
  white-space: nowrap;
  transition:
    background-color 0.24s ease,
    border-color 0.24s ease,
    box-shadow 0.24s ease,
    color 0.24s ease;
  cursor: pointer;
}

.header-item:hover {
  background: rgba(0, 212, 255, 0.12);
  border-color: rgba(0, 212, 255, 0.44);
  box-shadow: 0 0 0.8rem rgba(0, 212, 255, 0.2);
}

.item-text {
  font-family: 'Consolas', 'Monaco', monospace;
  color: var(--uav-text-primary);
  text-shadow: var(--uav-text-shadow);
}

.time-item .item-text {
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
}

.anchor-item {
  padding-right: clamp(0.72rem, 1vw, 1.05rem);
}

.mode-item.active {
  background: rgba(0, 212, 255, 0.14);
  border-color: rgba(0, 212, 255, 0.62);
  box-shadow: 0 0 0.9rem rgba(0, 212, 255, 0.22);
}

.mode-item.disabled {
  opacity: 0.42;
  cursor: not-allowed;
  pointer-events: none;
  box-shadow: none;
}

.mode-indicator {
  background: rgba(255, 255, 255, 0.03);
  border-color: rgba(255, 255, 255, 0.2);
}

.mode-indicator .item-text {
  color: #d6ecff;
  text-shadow: var(--uav-text-shadow);
}


/* 主内容区 */
.screen-main {
  flex: 1;
  position: relative;
  display: flex;
  justify-content: space-between;
  overflow: hidden;
}

/* 地图层 */
.map-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
}

.province-data-card {
  padding: 1rem;
  z-index: 130;
}

/* 遮罩层 - 优化为极致透明，不影响星空清晰度 */
.mask-layer {
  position: absolute;
  inset: 0;
  z-index: 10;
  background: radial-gradient(ellipse at center,
      rgba(0, 0, 0, 0) 0%,
      rgba(1, 6, 17, 0.05) 100%);
  pointer-events: none;
}

.uav-screen.is-global-view .mask-layer {
  background: radial-gradient(ellipse at center,
      rgba(0, 0, 0, 0) 0%,
      rgba(1, 6, 17, 0.02) 100%);
}

.right-panels,
.left-panels {
  margin-top: 8vh;
  width: 22vw;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 1rem;
  position: relative;
  z-index: 120;
}

.right-panels {
  margin-left: auto;
  height: calc(100% - 10vh - 1rem);
  overflow: hidden;
}

/* 左右面板 */
.left-panel {
  z-index: 100;
  min-height: 320px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.left-panel,
.right-panel {
  transition:
    width 0.28s ease,
    min-width 0.28s ease,
    max-width 0.28s ease,
    padding 0.28s ease;
}

.left-panel.is-collapsed {
  width: 64px;
  min-width: 64px;
  max-width: 64px;
  padding: 1rem 0.42rem;
  overflow: visible;
}

.right-panel {
  margin-left: auto;
  width: 100%;
  height: auto;
  min-height: 0;
  flex: 1;
  z-index: 100;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow: hidden;
}

.right-panel.is-collapsed {
  width: 64px;
  min-width: 64px;
  max-width: 64px;
  padding: 1rem 0.42rem;
  overflow: visible;
}

/* 自定义滚动条 */
.left-panel::-webkit-scrollbar,
.right-panel::-webkit-scrollbar {
  width: 6px;
}

.left-panel::-webkit-scrollbar-track,
.right-panel::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.left-panel::-webkit-scrollbar-thumb,
.right-panel::-webkit-scrollbar-thumb {
  background: var(--uav-primary);
  border-radius: 3px;
}

.left-panel::-webkit-scrollbar-thumb:hover,
.right-panel::-webkit-scrollbar-thumb:hover {
  background: var(--uav-success);
}

/* 底部统计条 */
.screen-footer {
  height: 10vh;
  min-height: 80px;
  position: relative;
  z-index: 100;
}

/* 响应式适配 - 1366x768 */
@media (max-width: 1440px) {
  .province-data-card {
    width: 100%;
  }

  .left-panel.is-collapsed,
  .right-panel.is-collapsed {
    width: 58px;
    min-width: 58px;
    max-width: 58px;
  }

  .screen-header {
    height: clamp(3.6rem, 6.8vh, 4.7rem);
    grid-template-columns: minmax(14.8rem, 24vw) minmax(0, 1fr) auto;
  }

  .screen-footer {
    height: 9vh;
    min-height: 72px;
  }

  .header-tabs {
    max-width: min(56vw, 56rem);
  }

  .header-status-group {
    gap: clamp(0.3rem, 0.6vw, 0.58rem);
    padding: clamp(0.16rem, 0.35vh, 0.28rem);
  }

  .tab-item {
    padding: 0 clamp(0.62rem, 1vw, 0.95rem);
  }
}

/* 响应式适配 - 小屏幕 */
@media (max-width: 1280px) {
  .left-panel,
  .right-panel {
    width: 25vw;
    min-width: 240px;
  }

  .left-panel.is-collapsed,
  .right-panel.is-collapsed {
    width: 54px;
    min-width: 54px;
    max-width: 54px;
  }

  .screen-header {
    height: clamp(3.4rem, 6.4vh, 4.35rem);
    grid-template-columns: minmax(12.8rem, 26vw) minmax(0, 1fr) auto;
    column-gap: clamp(0.35rem, 0.6vw, 0.7rem);
    padding: 0 clamp(0.5rem, 0.75vw, 0.85rem);
  }

  .header-title {
    font-size: clamp(0.98rem, 1.18vw, 1.2rem);
  }

  .header-subtitle {
    display: none;
  }

  .header-center {
    display: flex;
  }

  .header-tabs {
    max-width: min(50vw, 42rem);
    gap: clamp(0.3rem, 0.55vw, 0.55rem);
  }

  .tab-item {
    min-height: clamp(1.85rem, 2.9vh, 2.2rem);
    padding: 0 clamp(0.5rem, 0.75vw, 0.72rem);
  }

  .tab-text {
    max-width: clamp(4.2rem, 5.5vw, 5.4rem);
  }

  .header-status-group {
    gap: clamp(0.24rem, 0.45vw, 0.45rem);
    padding: clamp(0.12rem, 0.28vh, 0.22rem);
  }

  .header-item {
    min-height: clamp(1.85rem, 2.9vh, 2.2rem);
    padding: 0 clamp(0.4rem, 0.55vw, 0.6rem);
    font-size: clamp(0.72rem, 0.78vw, 0.82rem);
  }

  .header-item .item-text {
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
