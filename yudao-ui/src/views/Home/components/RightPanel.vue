<template>
  <div class="panel-container" :class="{ 'is-collapsed': props.collapsed }">
    <div v-if="props.collapsed" class="collapsed-placeholder" @click="toggleCollapse" title="展开右侧面板">
      <Icon icon="mdi:clipboard-list-outline" :size="20" />
      <span>任务面板</span>
    </div>

    <template v-else>
      <!-- 任务统计 -->
      <div class="mission-stats">
        <div class="panel-header">
          <button class="collapse-trigger" type="button" title="折叠右侧面板" @click="toggleCollapse">
            <span>−</span>
          </button>
          <div class="panel-title">
            <Icon icon="mdi:chart-donut" :size="20" />
            <span>任务统计</span>
          </div>
        </div>
        <div class="stats-content">
          <div class="stat-item" v-for="(item, index) in missionStats" :key="item.label"
            :style="{ animationDelay: `${index * 0.1}s` }">
            <div class="stat-bar">
              <div class="stat-bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
            </div>
            <div class="stat-info">
              <span class="stat-label">{{ item.label }}</span>
              <CountTo class="stat-number" :style="{ color: item.color }" :start-val="0" :end-val="item.value"
                :duration="2000" :decimals="0" />
            </div>
          </div>
        </div>
      </div>

      <!-- 任务列表 -->
      <div class="mission-list">
        <div class="panel-header">
          <div class="panel-title">
            <Icon icon="mdi:clipboard-list" :size="20" />
            <span>任务列表</span>
          </div>
        </div>
        <div class="list-content">
          <div class="list-item" v-for="(mission, index) in missions" :key="mission.id"
            :style="{ animationDelay: `${0.4 + index * 0.05}s` }">
            <div class="mission-info">
              <div class="mission-header">
                <span class="mission-name">{{ mission.name }}</span>
                <el-tag :type="getStatusType(mission.status)" size="small" effect="dark">
                  {{ mission.statusText }}
                </el-tag>
              </div>
              <div class="mission-details">
                <span class="detail-item">
                  <Icon icon="mdi:quadcopter" :size="14" />
                  {{ mission.deviceName }}
                </span>
                <span class="detail-item">
                  <Icon icon="mdi:clock-outline" :size="14" />
                  {{ mission.time }}
                </span>
              </div>
              <div class="mission-progress" v-if="mission.progress !== null">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: mission.progress + '%' }"></div>
                </div>
                <span class="progress-text">{{ mission.progress }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <OperationTrendChart class="operation-trend" />

    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { Icon } from '@/components/Icon'
import CountTo from '@/components/CountTo/src/CountTo.vue'
import type { DemoMission } from '../demo/mock-data'
import OperationTrendChart from './OperationTrendChart.vue'

defineOptions({ name: 'RightPanel' })

const props = withDefaults(
  defineProps<{
    collapsed?: boolean
    missionStats?: Array<{ label: string; value: number; percent: number; color: string }>
    missions?: DemoMission[]
  }>(),
  {
    collapsed: false,
    missionStats: () => [],
    missions: () => []
  }
)

const emit = defineEmits<{
  (e: 'toggle-collapse', collapsed: boolean): void
}>()

const toggleCollapse = () => {
  emit('toggle-collapse', !props.collapsed)
}
const missionStats = computed(() => props.missionStats)
const missions = computed(() => props.missions)

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    running: 'warning',
    pending: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped>
.panel-container {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  height: 100%;
}

.collapse-trigger {
  position: absolute;
  top: 10%;
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

/* 任务统计 */
.mission-stats {
  background: var(--uav-panel-bg);
  border-radius: 8px;
  overflow: hidden;
  backdrop-filter: var(--uav-panel-blur);
  animation: fadeInUp 0.6s ease-out backwards;
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

.stats-content {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  animation: fadeInRight 0.5s ease-out backwards;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.stat-bar {
  width: 100%;
  height: 8px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  overflow: hidden;
}

.stat-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}

.stat-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--uav-text-secondary);
  text-shadow: var(--uav-text-shadow);
}

.stat-number {
  font-size: 1rem;
  font-weight: 600;
  font-family: 'Consolas', monospace;
  text-shadow: var(--uav-text-shadow-strong);
}

/* 任务列表 */
.mission-list {
  flex: 1;
  min-height: 180px;
  background: var(--uav-panel-bg);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: var(--uav-panel-blur);
  animation: fadeInUp 0.6s ease-out 0.2s backwards;
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
  transition: all 0.3s ease;
  cursor: pointer;
  animation: fadeInRight 0.4s ease-out backwards;
}

.list-item:hover {
  background: rgba(0, 212, 255, 0.08);
  transform: translateX(-4px);
}

.list-item:last-child {
  margin-bottom: 0;
}

.mission-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.mission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.mission-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--uav-text-primary);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: var(--uav-text-shadow-strong);
}

.mission-details {
  display: flex;
  gap: 1rem;
  font-size: 0.75rem;
  color: var(--uav-text-secondary);
  text-shadow: var(--uav-text-shadow);
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.mission-progress {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--uav-primary) 0%, var(--uav-success) 100%);
  border-radius: 3px;
  transition: width 0.6s ease;
  animation: progressPulse 2s ease-in-out infinite;
}

@keyframes progressPulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.8;
  }
}

.progress-text {
  font-size: 0.75rem;
  color: var(--uav-primary);
  font-family: 'Consolas', monospace;
  min-width: 3em;
  text-align: right;
  font-weight: 600;
  text-shadow: var(--uav-text-shadow);
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
  .stats-content {
    padding: 0.75rem;
    gap: 0.75rem;
  }

  .list-item {
    padding: 0.625rem;
  }

  .mission-name {
    font-size: 0.8125rem;
  }
}
</style>
