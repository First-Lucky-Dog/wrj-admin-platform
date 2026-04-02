<template>
  <section class="operation-trend-card">
    <div class="panel-header">
      <div class="panel-title">
        <Icon icon="mdi:chart-line" :size="20" />
        <span>运行趋势</span>
        <span class="panel-subtitle">/ Operational trend</span>
      </div>
    </div>

    <div class="trend-toolbar">
      <div class="custom-legend">
        <div class="legend-item">
          <span class="legend-dot warning"></span>
          <span>预警数量</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot detect"></span>
          <span>探测数量</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot intrusion"></span>
          <span>未授权数量</span>
        </div>
      </div>

      <div class="range-switch">
        <button
          v-for="item in rangeOptions"
          :key="item.value"
          type="button"
          class="switch-btn"
          :class="{ active: activeRange === item.value }"
          @click="setRange(item.value)"
        >
          {{ item.label }}
        </button>
      </div>
    </div>

    <div
      class="trend-chart-wrapper"
      @mouseenter="handleChartMouseEnter"
      @mouseleave="handleChartMouseLeave"
    >
      <Echart ref="chartRef" class="trend-chart" :options="chartOptions" height="250px" />
    </div>
  </section>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import type { EChartsOption } from 'echarts'
import Echart from '@/components/Echart/src/Echart.vue'
import { Icon } from '@/components/Icon'

defineOptions({ name: 'OperationTrendChart' })

type RangeType = 'yesterday' | 'today' | 'week'

interface TrendDataSet {
  warning: number[]
  detect: number[]
  intrusion: number[]
}

interface EchartExpose {
  dispatchAction: (payload: Record<string, unknown>) => void
}

const rangeOptions: { label: string; value: RangeType }[] = [
  { label: '昨日', value: 'yesterday' },
  { label: '今日', value: 'today' },
  { label: '近7日', value: 'week' }
]

const WINDOW_SIZE = 7
const REALTIME_UPDATE_MIN_MS = 2000
const REALTIME_UPDATE_MAX_MS = 3000

const formatTimeLabel = (timestamp: number) => {
  const date = new Date(timestamp)
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${hh}:${mm}:${ss}`
}

const createInitialAxis = () => {
  const now = Date.now()
  const step = 30_000
  return Array.from({ length: WINDOW_SIZE }, (_, index) =>
    formatTimeLabel(now - step * (WINDOW_SIZE - 1 - index))
  )
}

const xAxisData = ref<string[]>(createInitialAxis())

const trendDataMap = ref<Record<RangeType, TrendDataSet>>({
  yesterday: {
    warning: [180, 52, 58, 50, 150, 48, 156],
    detect: [140, 42, 80, 120, 138, 74, 52],
    intrusion: [82, 74, 36, 48, 132, 110, 56]
  },
  today: {
    warning: [168, 58, 62, 56, 145, 56, 148],
    detect: [132, 46, 86, 118, 142, 82, 58],
    intrusion: [78, 70, 40, 54, 125, 102, 60]
  },
  week: {
    warning: [152, 60, 72, 66, 138, 72, 132],
    detect: [118, 54, 90, 112, 130, 88, 74],
    intrusion: [70, 62, 46, 58, 112, 96, 68]
  }
})

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value))

const createNextValue = (lastValue: number, min: number, max: number, delta = 18) => {
  const randomDelta = (Math.random() - 0.5) * 2 * delta
  return Math.round(clamp(lastValue + randomDelta, min, max))
}

const pushRollingValue = (series: number[], next: number) => {
  const nextSeries = series.slice(1)
  nextSeries.push(next)
  return nextSeries
}

const nextTrendData = (source: TrendDataSet, delta: number): TrendDataSet => ({
  warning: pushRollingValue(
    source.warning,
    createNextValue(source.warning[source.warning.length - 1] ?? 120, 40, 190, delta)
  ),
  detect: pushRollingValue(
    source.detect,
    createNextValue(source.detect[source.detect.length - 1] ?? 100, 30, 170, delta - 2)
  ),
  intrusion: pushRollingValue(
    source.intrusion,
    createNextValue(source.intrusion[source.intrusion.length - 1] ?? 80, 20, 145, delta - 4)
  )
})

const activeRange = ref<RangeType>('yesterday')
const chartRef = ref<EchartExpose | null>(null)
const isChartHovering = ref(false)

const syncTooltipToLatest = () => {
  if (isChartHovering.value) return
  const latestIndex = xAxisData.value.length - 1
  if (latestIndex < 0) return
  chartRef.value?.dispatchAction({
    type: 'showTip',
    seriesIndex: 0,
    dataIndex: latestIndex
  })
}

const scheduleTooltipSync = () => {
  nextTick(() => {
    syncTooltipToLatest()
  })
}

const handleChartMouseEnter = () => {
  isChartHovering.value = true
}

const handleChartMouseLeave = () => {
  isChartHovering.value = false
  scheduleTooltipSync()
}

const setRange = (range: RangeType) => {
  activeRange.value = range
  scheduleTooltipSync()
}

const currentData = computed(() => trendDataMap.value[activeRange.value])

const yAxisMax = computed(() => {
  const maxValue = Math.max(
    ...currentData.value.warning,
    ...currentData.value.detect,
    ...currentData.value.intrusion
  )
  const rounded = Math.ceil(maxValue / 36) * 36
  return Math.max(180, rounded)
})

const chartOptions = computed<EChartsOption>(() => ({
  color: ['#4ef2ff', '#20f1b4', '#6d84b3'],
  animationDuration: 700,
  animationDurationUpdate: 1200,
  animationEasing: 'cubicOut',
  animationEasingUpdate: 'cubicInOut',
  grid: {
    top: 22,
    right: 16,
    bottom: 22,
    left: 44
  },
  tooltip: {
    trigger: 'axis',
    triggerOn: 'mousemove|click',
    alwaysShowContent: true,
    confine: true,
    backgroundColor: 'rgba(3, 8, 18, 0.92)',
    borderColor: 'rgba(127, 224, 255, 0.28)',
    borderWidth: 1,
    textStyle: {
      color: '#d9f6ff',
      fontSize: 14
    },
    axisPointer: {
      type: 'line',
      snap: true,
      lineStyle: {
        color: 'rgba(170, 214, 255, 0.55)',
        type: 'dashed',
        width: 1
      }
    },
    formatter: (params: any) => {
      const rows = params
        .map((item: any) => {
          return `<div style="display:flex;align-items:center;gap:8px;line-height:1.8;">
            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${item.color};"></span>
            <span>${item.seriesName}：<b>${item.value}</b></span>
          </div>`
        })
        .join('')
      return rows
    }
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: xAxisData.value,
    axisTick: { show: false },
    axisLine: { show: false },
    axisLabel: {
      color: 'rgba(206, 229, 255, 0.7)',
      fontSize: 12
    }
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: yAxisMax.value,
    splitNumber: 5,
    axisTick: { show: false },
    axisLine: { show: false },
    axisLabel: {
      color: 'rgba(206, 229, 255, 0.7)',
      fontSize: 12
    },
    splitLine: {
      show: true,
      lineStyle: {
        color: 'rgba(151, 188, 221, 0.28)',
        width: 1,
        type: 'dashed'
      }
    }
  },
  series: [
    {
      name: '预警数量',
      type: 'line',
      smooth: 0.4,
      data: currentData.value.warning,
      symbol: 'none',
      lineStyle: { width: 3 },
      emphasis: { focus: 'series' }
    },
    {
      name: '探测数量',
      type: 'line',
      smooth: 0.4,
      data: currentData.value.detect,
      symbol: 'none',
      lineStyle: { width: 3 },
      emphasis: { focus: 'series' }
    },
    {
      name: '未授权数量',
      type: 'line',
      smooth: 0.4,
      data: currentData.value.intrusion,
      symbol: 'none',
      lineStyle: { width: 3 },
      emphasis: { focus: 'series' }
    }
  ]
}))

let realtimeTimer: ReturnType<typeof setTimeout> | null = null

const refreshRealtimeData = () => {
  const now = Date.now()
  xAxisData.value = [...xAxisData.value.slice(1), formatTimeLabel(now)]
  trendDataMap.value = {
    yesterday: nextTrendData(trendDataMap.value.yesterday, 16),
    today: nextTrendData(trendDataMap.value.today, 14),
    week: nextTrendData(trendDataMap.value.week, 10)
  }
}

const scheduleRealtimeUpdate = () => {
  const delay =
    REALTIME_UPDATE_MIN_MS +
    Math.floor(Math.random() * (REALTIME_UPDATE_MAX_MS - REALTIME_UPDATE_MIN_MS + 1))
  realtimeTimer = setTimeout(() => {
    refreshRealtimeData()
    scheduleRealtimeUpdate()
  }, delay)
}

onMounted(() => {
  scheduleRealtimeUpdate()
  scheduleTooltipSync()
})

onUnmounted(() => {
  if (realtimeTimer) {
    clearTimeout(realtimeTimer)
    realtimeTimer = null
  }
})

watch(xAxisData, () => {
  scheduleTooltipSync()
})
</script>

<style scoped>
.operation-trend-card {
  width: 100%;
  background: var(--uav-panel-bg);
  border-radius: 8px;
  backdrop-filter: var(--uav-panel-blur);
  animation: fadeInUp 0.6s ease-out 0.1s backwards;
}

.panel-header {
  padding: 1rem;
  background: rgba(0, 212, 255, 0.03);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--uav-primary);
  text-shadow: var(--uav-text-shadow-strong);
}

.panel-subtitle {
  margin-left: 0.6rem;
  font-size: 0.82rem;
  color: var(--uav-text-secondary);
  font-family: 'Consolas', 'Monaco', monospace;
  font-weight: 400;
}

.trend-toolbar {
  padding: 0.65rem 0.1rem 0.3rem;
  display: flex;
  /* align-items: center; */
  gap: 0.5rem;
}

.custom-legend {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.32rem;
  color: var(--uav-text-secondary);
  font-size: 0.8rem;
  white-space: nowrap;
  flex-shrink: 0;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
}

.legend-dot.warning {
  background: #4ef2ff;
}

.legend-dot.detect {
  background: #20f1b4;
}

.legend-dot.intrusion {
  background: #6d84b3;
}

.range-switch {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  flex-shrink: 0;
}

.switch-btn {
  border: 1px solid rgba(148, 196, 236, 0.35);
  border-radius: 8px;
  background: rgba(7, 16, 35, 0.45);
  color: rgba(210, 230, 255, 0.75);
  padding: 0.16rem 0.42rem;
  font-size: 0.78rem;
  line-height: 1.2;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;
}

.switch-btn:hover {
  border-color: rgba(148, 222, 255, 0.56);
  color: #e8f6ff;
}

.switch-btn.active {
  border-color: rgba(201, 236, 255, 0.9);
  color: #ffffff;
  background: rgba(12, 22, 45, 0.75);
  box-shadow: 0 0 12px rgba(91, 186, 255, 0.22);
}

.trend-chart-wrapper {
  width: 100%;
}

.trend-chart {
  padding: 0 0.5rem 0.2rem;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
