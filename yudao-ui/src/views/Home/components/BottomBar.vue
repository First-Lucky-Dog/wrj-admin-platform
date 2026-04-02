<template>
  <div class="bottom-bar">
    <div
      class="stat-item"
      v-for="(item, index) in stats"
      :key="item.label"
      :style="{ animationDelay: `${index * 0.1}s` }"
    >
      <div class="stat-icon" :style="{ color: item.color }">
        <Icon :icon="item.icon" :size="28" />
      </div>
      <div class="stat-content">
        <span class="stat-label">{{ item.label }}</span>
        <div class="stat-value-wrapper">
          <CountTo
            class="stat-value"
            :start-val="0"
            :end-val="item.value"
            :duration="2500"
            :decimals="0"
            :separator="item.separator || ''"
          />
          <span class="stat-unit">{{ item.unit }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Icon } from '@/components/Icon'
import CountTo from '@/components/CountTo/src/CountTo.vue'

defineOptions({ name: 'BottomBar' })

// 统计数据
const stats = ref([
  {
    label: '飞行总里程',
    value: 12458,
    unit: 'km',
    icon: 'mdi:map-marker-distance',
    color: '#00d4ff',
    separator: ','
  },
  {
    label: '飞行总时长',
    value: 3256,
    unit: 'h',
    icon: 'mdi:clock-time-eight',
    color: '#00ff88',
    separator: ','
  },
  {
    label: '完成任务数',
    value: 1847,
    unit: '次',
    icon: 'mdi:check-circle',
    color: '#ffaa00',
    separator: ','
  },
  {
    label: '今日飞行次数',
    value: 28,
    unit: '次',
    icon: 'mdi:airplane-takeoff',
    color: '#ff4d4f',
    separator: ''
  }
])

// 定时器：模拟数据增长
let growthTimer: number | null = null

// 模拟数据增长
const simulateGrowth = () => {
  // 飞行总里程随机增加 1-5 km
  stats.value[0].value += Math.floor(Math.random() * 5) + 1

  // 飞行总时长随机增加 0-2 h
  stats.value[1].value += Math.floor(Math.random() * 3)

  // 完成任务数随机增加 0-1
  if (Math.random() > 0.7) {
    stats.value[2].value += 1
  }

  // 今日飞行次数随机增加 0-1
  if (Math.random() > 0.8) {
    stats.value[3].value += 1
  }
}

onMounted(() => {
  // 每10秒模拟数据增长
  growthTimer = window.setInterval(simulateGrowth, 10000)
})

onUnmounted(() => {
  if (growthTimer) {
    clearInterval(growthTimer)
    growthTimer = null
  }
})
</script>

<style scoped>
.bottom-bar {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 0 2%;
  background: var(--uav-panel-bg);
  border-top: 2px solid rgba(0, 212, 255, var(--uav-border-opacity));
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.3), var(--uav-border-glow);
  backdrop-filter: var(--uav-panel-blur);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1.5rem;
  background: rgba(0, 212, 255, 0.03);
  border: 1px solid rgba(0, 212, 255, var(--uav-border-opacity));
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  min-width: 0;
  flex: 1;
  max-width: 280px;
  box-shadow: var(--uav-border-glow);
  animation: slideInUp 0.6s ease-out backwards;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-item:hover {
  background: rgba(0, 212, 255, 0.08);
  border-color: var(--uav-primary);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.5);
  transform: translateY(-2px);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: rgba(0, 212, 255, 0.1);
  border-radius: 8px;
  flex-shrink: 0;
  animation: iconPulse 3s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 rgba(0, 212, 255, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 20px rgba(0, 212, 255, 0.4);
  }
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--uav-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: var(--uav-text-shadow);
}

.stat-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--uav-text-primary);
  font-family: 'Consolas', monospace;
  line-height: 1;
  text-shadow: var(--uav-text-shadow-strong);
}

.stat-unit {
  font-size: 0.875rem;
  color: var(--uav-text-secondary);
  font-weight: 400;
  text-shadow: var(--uav-text-shadow);
}

/* 响应式适配 - 1366x768 */
@media (max-width: 1440px) {
  .bottom-bar {
    padding: 0 1.5%;
  }

  .stat-item {
    padding: 0.625rem 1.25rem;
    gap: 0.75rem;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-label {
    font-size: 0.8125rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .stat-unit {
    font-size: 0.8125rem;
  }
}

/* 响应式适配 - 小屏幕 */
@media (max-width: 1280px) {
  .bottom-bar {
    padding: 0 1%;
  }

  .stat-item {
    padding: 0.5rem 1rem;
    gap: 0.625rem;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
  }

  .stat-label {
    font-size: 0.75rem;
  }

  .stat-value {
    font-size: 1.25rem;
  }

  .stat-unit {
    font-size: 0.75rem;
  }
}

/* 超小屏幕 - 隐藏图标 */
@media (max-width: 1024px) {
  .stat-icon {
    display: none;
  }

  .stat-item {
    justify-content: center;
  }

  .stat-content {
    align-items: center;
  }
}
</style>
