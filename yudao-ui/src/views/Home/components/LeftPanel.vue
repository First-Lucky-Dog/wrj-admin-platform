<template>
  <div class="panel-container" :class="{ 'is-collapsed': props.collapsed }">
    <div v-if="props.collapsed" class="collapsed-placeholder" @click="toggleCollapse" title="展开左侧面板">
      <Icon icon="mdi:quadcopter" :size="20" />
      <span>无人机面板</span>
    </div>

    <template v-else>
      <!-- 筛选控制区 -->
      <div class="filter-section">
        <div class="panel-header">
          <div class="panel-title">
            <Icon icon="mdi:filter-variant" :size="20" />
            <span>筛选控制</span>
          </div>
          <button
            class="collapse-trigger"
            type="button"
            title="折叠左侧面板"
            @click="toggleCollapse"
          >
            <span>−</span>
          </button>
        </div>
        <div class="filter-content">
          <!-- 未授权无人机筛选 -->
          <div class="filter-row illegal-filter">
            <el-switch
              :model-value="showIllegalOnly"
              :active-icon="AlertIcon"
              active-color="#ff2d2d"
              inactive-color="rgba(255,255,255,0.2)"
              @change="handleIllegalFilterChange"
            />
            <span class="filter-label" :class="{ active: showIllegalOnly }">
              <Icon icon="mdi:alert-circle" :size="16" />
              只显示未授权无人机
            </span>
          </div>
          <!-- 类型筛选 -->
          <div class="filter-categories">
            <div
              class="category-tag"
              v-for="cat in categoryOptions"
              :key="cat.value"
              :class="{ active: activeCategorySet.has(cat.value), disabled: showIllegalOnly }"
              @click="toggleCategory(cat.value)"
            >
              <Icon :icon="cat.icon" :size="14" />
              <span>{{ cat.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 设备列表 -->
      <div class="device-list">
        <div class="panel-header">
          <div class="panel-title">
            <Icon icon="mdi:quadcopter" :size="20" />
            <span>无人机列表</span>
            <span class="list-count">({{ filteredDevices.length }})</span>
          </div>
        </div>
        <div class="list-content">
          <div
            class="list-item"
            :class="{
              'is-active': selectedDroneId === device.id,
              'is-illegal': device.isIllegal
            }"
            v-for="(device, index) in filteredDevices"
            :key="device.id"
            :style="{ animationDelay: `${0.4 + index * 0.05}s` }"
            @click="handleDeviceClick(device)"
          >
            <div class="device-info">
              <span class="device-name" :class="{ 'illegal-name': device.isIllegal }">
                <span v-if="device.isIllegal" class="illegal-badge">未授权无人机</span>
                {{ device.name }}
              </span>
              <span class="device-model">
                {{ device.model }}
                <span class="device-category">· {{ getCategoryLabel(device.category) }}</span>
              </span>
            </div>
            <div class="device-status">
              <el-tag
                :type="device.isIllegal ? 'danger' : device.status === 'online' ? 'success' : device.status === 'flying' ? 'warning' : 'info'"
                size="small"
                effect="dark"
              >
                {{ device.statusText }}
              </el-tag>
              <span class="battery-level" v-if="device.battery !== null" :class="getBatteryClass(device.battery)">
                <Icon icon="mdi:battery" :size="16" />
                {{ device.battery }}%
              </span>
            </div>
          </div>
          <div v-if="filteredDevices.length === 0" class="empty-list">
            <Icon icon="mdi:alert-circle-outline" :size="24" />
            <span>无匹配设备</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed, h } from 'vue'
import { Icon } from '@/components/Icon'
import { ALL_DRONE_CATEGORIES, type DemoDrone, type DroneCategory } from '../demo/mock-data'

defineOptions({ name: 'LeftPanel' })

// 自定义图标组件（用于 el-switch）
const AlertIcon = h('span', { style: 'color: #fff; font-size: 12px;' }, '!')

// 类型选项配置
const categoryOptions: { value: DroneCategory; label: string; icon: string }[] = [
  { value: 'logistics', label: '物流', icon: 'mdi:truck-delivery' },
  { value: 'commercial', label: '商用', icon: 'mdi:domain' },
  { value: 'public', label: '公共设施', icon: 'mdi:city-variant' },
  { value: 'traffic', label: '交通', icon: 'mdi:traffic-light' }
]

const props = withDefaults(
  defineProps<{
    collapsed?: boolean
    drones?: DemoDrone[]
    selectedDroneId?: number | null
    showIllegalOnly?: boolean
    activeCategories?: DroneCategory[]
  }>(),
  {
    collapsed: false,
    drones: () => [],
    selectedDroneId: null,
    showIllegalOnly: false,
    activeCategories: () => [...ALL_DRONE_CATEGORIES]
  }
)

interface DroneSelectPayload {
  id: number
  name: string
}

const emit = defineEmits<{
  (e: 'select-drone', payload: DroneSelectPayload): void
  (e: 'toggle-collapse', collapsed: boolean): void
  (e: 'filter-category', categories: DroneCategory[]): void
  (e: 'filter-illegal', showIllegalOnly: boolean): void
}>()

const toggleCollapse = () => {
  emit('toggle-collapse', !props.collapsed)
}

const showIllegalOnly = computed(() => props.showIllegalOnly)
const activeCategorySet = computed(() => new Set(props.activeCategories))
const selectedDroneId = computed(() => props.selectedDroneId)

// 切换类型筛选
const toggleCategory = (category: DroneCategory) => {
  if (showIllegalOnly.value) return

  const nextCategories = new Set(props.activeCategories)
  if (nextCategories.has(category)) {
    // 至少保留一个类型
    if (nextCategories.size > 1) {
      nextCategories.delete(category)
    }
  } else {
    nextCategories.add(category)
  }
  emit('filter-category', Array.from(nextCategories))
}

// 切换未授权无人机筛选
const handleIllegalFilterChange = (value: boolean) => {
  emit('filter-illegal', value)
}

// 根据筛选条件过滤设备列表
const filteredDevices = computed(() => {
  return props.drones.filter((device) => {
    // 只显示未授权无人机模式
    if (showIllegalOnly.value) {
      return device.isIllegal
    }
    // 按类型筛选
    return activeCategorySet.value.has(device.category)
  })
})

const handleDeviceClick = (device: DemoDrone) => {
  emit('select-drone', {
    id: device.id,
    name: device.name
  })
}

// 获取电池电量样式
const getBatteryClass = (battery: number) => {
  if (battery <= 20) return 'battery-low'
  if (battery <= 50) return 'battery-medium'
  return 'battery-high'
}

// 获取类型标签
const getCategoryLabel = (category: DroneCategory) => {
  const found = categoryOptions.find(opt => opt.value === category)
  return found ? found.label : '未知'
}
</script>

<style scoped>
.panel-container {
  display: flex;
  flex-direction: column;
}

.collapse-trigger {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 1.5rem;
  height: 1.5rem;
  border: none;
  border-radius: 4px;
  background: rgba(10, 20, 42, 0.8);
  color: var(--uav-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  font-size: 1.2rem;
  font-weight: bold;
  line-height: 1;
}

.collapse-trigger:hover {
  color: #ffffff;
  background: rgba(0, 212, 255, 0.2);
}

.panel-container.is-collapsed {
  gap: 0;
  align-items: center;
}

.collapsed-placeholder {
  margin-top: 3.1rem;
  padding: 0.8rem 0.35rem;
  border-radius: 0.6rem;
  background: rgba(0, 212, 255, 0.08);
  color: var(--uav-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.75rem;
  line-height: 1.2;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  cursor: pointer;
  transition: all 0.25s ease;
}

.collapsed-placeholder:hover {
  background: rgba(0, 212, 255, 0.15);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 设备列表 */
.device-list {
  flex: 1;
  background: var(--uav-panel-bg);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: var(--uav-panel-blur);
  animation: fadeInUp 0.6s ease-out 0.3s backwards;
}

.panel-header {
  position: relative;
  padding: 1rem;
  background: rgba(0, 212, 255, 0.03);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--uav-primary);
  text-shadow: var(--uav-text-shadow-strong);
}

.list-content {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.list-item {
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  background: rgba(0, 212, 255, 0.03);
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
  animation: fadeInLeft 0.4s ease-out backwards;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.list-item:hover {
  background: rgba(0, 212, 255, 0.08);
  transform: translateX(4px);
}

.list-item.is-active {
  background: rgba(0, 212, 255, 0.1);
}

.list-item:last-child {
  margin-bottom: 0;
}

.device-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
  min-width: 0;
}

.device-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--uav-text-primary);
  font-family: 'Consolas', monospace;
  text-shadow: var(--uav-text-shadow-strong);
}

.device-model {
  font-size: 0.75rem;
  color: var(--uav-text-secondary);
  text-shadow: var(--uav-text-shadow);
}

.device-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
}

.battery-level {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-family: 'Consolas', monospace;
  transition: all 0.3s ease;
}

.battery-high {
  color: var(--uav-success);
}

.battery-medium {
  color: var(--uav-warning);
}

.battery-low {
  color: var(--uav-danger);
  animation: batteryBlink 1s ease-in-out infinite;
}

@keyframes batteryBlink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 自定义滚动条 */
.list-content::-webkit-scrollbar {
  width: 4px;
}

.list-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}

.list-content::-webkit-scrollbar-thumb {
  background: var(--uav-primary);
  border-radius: 2px;
}

.list-content::-webkit-scrollbar-thumb:hover {
  background: var(--uav-success);
}

/* 响应式适配 */
@media (max-width: 1440px) {
  .panel-container {
  }

  .list-item {
    padding: 0.625rem;
  }
}

/* 筛选控制区 */
.filter-section {
  background: var(--uav-panel-bg);
  border-radius: 8px;
  overflow: hidden;
  backdrop-filter: var(--uav-panel-blur);
  animation: fadeInUp 0.6s ease-out backwards;
}

.filter-content {
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.illegal-filter {
  padding: 0.25rem 0;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.875rem;
  color: var(--uav-text-secondary);
  transition: all 0.3s ease;
  text-shadow: var(--uav-text-shadow);
}

.filter-label.active {
  color: #ff2d2d;
  font-weight: 600;
  text-shadow: var(--uav-text-shadow-strong);
}

.filter-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.category-tag {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.35rem 0.6rem;
  background: rgba(0, 212, 255, 0.08);
  border-radius: 4px;
  font-size: 0.75rem;
  color: var(--uav-text-secondary);
  cursor: pointer;
  transition: all 0.25s ease;
}

.category-tag:hover:not(.disabled) {
  background: rgba(0, 212, 255, 0.15);
}

.category-tag.active {
  background: rgba(0, 212, 255, 0.2);
  color: var(--uav-primary);
}

.category-tag.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 未授权无人机设备样式 */
.list-item.is-illegal {
  background: rgba(255, 45, 45, 0.1);
  animation: illegalPulse 2s ease-in-out infinite;
}

.list-item.is-illegal:hover {
  background: rgba(255, 45, 45, 0.18);
}

.list-item.is-illegal.is-active {
  background: rgba(255, 45, 45, 0.22);
}

@keyframes illegalPulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.85;
  }
}

.device-name.illegal-name {
  color: #ff6b6b;
}

.illegal-badge {
  display: inline-block;
  background: #ff2d2d;
  color: #ffffff;
  font-size: 0.625rem;
  padding: 0.1rem 0.3rem;
  border-radius: 3px;
  margin-right: 0.3rem;
  font-weight: 600;
  vertical-align: middle;
}

.device-category {
  color: rgba(255, 255, 255, 0.45);
  font-size: 0.7rem;
}

.list-count {
  margin-left: 0.35rem;
  font-size: 0.8rem;
  color: var(--uav-text-secondary);
  font-weight: 400;
  text-shadow: var(--uav-text-shadow);
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem 0;
  color: var(--uav-text-secondary);
  font-size: 0.8rem;
  text-shadow: var(--uav-text-shadow);
}
</style>
