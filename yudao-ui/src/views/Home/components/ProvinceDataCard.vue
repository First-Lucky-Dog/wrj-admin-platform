<template>
  <section class="province-card">
    <header class="card-header">
      <div class="header-main">
        <Icon icon="mdi:database-outline" :size="18" />
        <span>全省数据</span>
      </div>
      <span class="header-sub">Provincial data</span>
    </header>

    <div class="card-body">
      <div class="coverage-ring" :style="{ '--coverage': `${coveragePercent}%` }">
        <div class="ring-inner">
          <span class="ring-value">{{ coveragePercent }}%</span>
        </div>
      </div>

      <div class="data-panel">
        <div class="main-metric">
          <CountTo
            class="main-value"
            :start-val="0"
            :end-val="totalDevices"
            :duration="2200"
            :decimals="0"
          />
          <span class="main-unit">架</span>
        </div>
        <div class="main-label">接入设备</div>

        <div class="data-list">
          <div class="data-item">
            <span class="dot online"></span>
            <span class="label">在线设备</span>
            <CountTo :start-val="0" :end-val="onlineDevices" :duration="2000" :decimals="0" />
          </div>
          <div class="data-item">
            <span class="dot flying"></span>
            <span class="label">执行任务</span>
            <CountTo :start-val="0" :end-val="flyingDevices" :duration="2000" :decimals="0" />
          </div>
          <div class="data-item">
            <span class="dot illegal"></span>
            <span class="label">未授权告警</span>
            <CountTo :start-val="0" :end-val="illegalDevices" :duration="2000" :decimals="0" />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { Icon } from '@/components/Icon'
import CountTo from '@/components/CountTo/src/CountTo.vue'

defineOptions({ name: 'ProvinceDataCard' })

interface StatItem {
  label: string
  value: number
  icon: string
  color: string
}

const props = withDefaults(
  defineProps<{
    stats?: StatItem[]
  }>(),
  {
    stats: () => []
  }
)

const statMap = computed(() => {
  const mapping = new Map<string, number>()
  props.stats.forEach((item) => {
    mapping.set(item.label, item.value)
  })
  return mapping
})

const resolveStat = (label: string, fallbackIndex: number) =>
  statMap.value.get(label) ?? props.stats[fallbackIndex]?.value ?? 0

const totalDevices = computed(() => resolveStat('总设备数', 0))
const onlineDevices = computed(() => resolveStat('在线设备', 1))
const flyingDevices = computed(() => resolveStat('执行任务', 2))
const illegalDevices = computed(() => resolveStat('未授权无人机告警', 3))
const coveragePercent = computed(() => {
  if (totalDevices.value <= 0) return 0
  return Math.round((onlineDevices.value / totalDevices.value) * 100)
})
</script>

<style scoped>
.province-card {
  border-radius: 8px;
  background: var(--uav-panel-bg);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.75rem 0.9rem;
  background: rgba(0, 212, 255, 0.03);
  border-bottom: 1px solid rgba(0, 212, 255, var(--uav-border-opacity, 0.7));
}

.header-main {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  color: var(--uav-primary, #00d4ff);
  font-size: 1rem;
  font-weight: 700;
  text-shadow: var(--uav-text-shadow-strong);
}

.header-sub {
  color: var(--uav-text-secondary);
  font-size: 0.72rem;
  letter-spacing: 0.03em;
  font-family: 'Consolas', 'Monaco', monospace;
  text-shadow: var(--uav-text-shadow);
}

.card-body {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 0.8rem;
  padding: 0.9rem;
}

.coverage-ring {
  --coverage: 0%;
  width: 104px;
  height: 104px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    conic-gradient(#6bf0ff var(--coverage), rgba(80, 116, 150, 0.28) var(--coverage)),
    radial-gradient(circle at center, rgba(0, 18, 40, 0.9), rgba(0, 8, 20, 0.92));
  box-shadow:
    0 0 16px rgba(0, 210, 255, 0.28),
    inset 0 0 12px rgba(0, 140, 200, 0.36);
}

.ring-inner {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  border: 1px solid rgba(141, 238, 255, 0.35);
  background: radial-gradient(circle at 30% 30%, rgba(77, 212, 255, 0.45), rgba(4, 34, 58, 0.88));
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-value {
  color: #95eeff;
  font-size: 1.15rem;
  font-weight: 700;
  font-family: 'Consolas', 'Monaco', monospace;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.45);
}

.data-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.main-metric {
  display: flex;
  align-items: baseline;
  gap: 0.2rem;
}

.main-value {
  font-size: 2rem;
  line-height: 1;
  color: var(--uav-text-primary);
  font-weight: 700;
  text-shadow: var(--uav-text-shadow-strong);
}

.main-unit {
  font-size: 0.9rem;
  color: var(--uav-text-secondary);
  font-family: 'Consolas', 'Monaco', monospace;
  text-shadow: var(--uav-text-shadow);
}

.main-label {
  font-size: 0.82rem;
  color: var(--uav-text-secondary);
  text-shadow: var(--uav-text-shadow);
}

.data-list {
  margin-top: 0.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.data-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 0.45rem;
  color: var(--uav-text-primary);
  font-size: 0.84rem;
  text-shadow: var(--uav-text-shadow);
}

.label {
  color: var(--uav-text-secondary);
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  box-shadow: 0 0 8px currentColor;
}

.dot.online {
  color: #63f0ff;
  background: #63f0ff;
}

.dot.flying {
  color: #ffd166;
  background: #ffd166;
}

.dot.illegal {
  color: #ff595e;
  background: #ff595e;
}
</style>
