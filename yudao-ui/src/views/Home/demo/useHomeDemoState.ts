import { computed, onMounted, onUnmounted, ref } from 'vue'
import {
  DEMO_DRONES_SEED,
  DEMO_MISSIONS_SEED,
  type DemoDrone,
  type DemoMission,
  type DroneStatus
} from './mock-data'

interface PanelStat {
  label: string
  value: number
  icon: string
  color: string
}

interface MissionStat {
  label: string
  value: number
  percent: number
  color: string
}

interface DroneMotionProfile {
  centerLng: number
  centerLat: number
  baseAlt: number
  phase: number
  angularSpeed: number
  radiusLng: number
  radiusLat: number
  altitudePhase: number
}

const STATUS_TEXT_MAP: Record<DroneStatus, string> = {
  flying: '飞行中',
  online: '在线',
  offline: '离线'
}

const cloneDroneSeed = (): DemoDrone[] => DEMO_DRONES_SEED.map((item) => ({ ...item }))
const cloneMissionSeed = (): DemoMission[] => DEMO_MISSIONS_SEED.map((item) => ({ ...item }))

const calcPercent = (value: number, total: number) => {
  if (total <= 0) return 0
  return Math.round((value / total) * 100)
}

const clampNumber = (value: number, min: number, max: number) => {
  if (value < min) return min
  if (value > max) return max
  return value
}

const seededNoise = (seed: number) => {
  const x = Math.sin(seed * 127.1) * 43758.5453123
  return x - Math.floor(x)
}

const buildMotionProfile = (drone: DemoDrone): DroneMotionProfile => {
  const n1 = seededNoise(drone.id * 1.37)
  const n2 = seededNoise(drone.id * 2.17)
  const n3 = seededNoise(drone.id * 3.91)
  const n4 = seededNoise(drone.id * 5.03)

  return {
    centerLng: drone.lng,
    centerLat: drone.lat,
    baseAlt: Math.max(180, drone.alt || 220),
    phase: n1 * Math.PI * 2,
    angularSpeed: 0.04 + n2 * 0.04,
    radiusLng: 0.01 + n3 * 0.03,
    radiusLat: 0.01 + n4 * 0.024,
    altitudePhase: n2 * Math.PI * 2
  }
}

const getDroneStatusText = (drone: DemoDrone) => {
  if (drone.isIllegal) {
    return '未授权无人机'
  }
  return STATUS_TEXT_MAP[drone.status]
}

const formatMissionTime = (date: Date) => {
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

export const useHomeDemoState = () => {
  const drones = ref<DemoDrone[]>(cloneDroneSeed())
  const missions = ref<DemoMission[]>(cloneMissionSeed())
  const motionProfileMap = new Map<number, DroneMotionProfile>()
  let missionTimer: number | null = null
  let droneTelemetryTimer: number | null = null
  let droneStatusTimer: number | null = null
  let missionRescheduleTimer: number | null = null

  const droneStats = computed<PanelStat[]>(() => {
    const total = drones.value.length
    const online = drones.value.filter((item) => item.status === 'online' || item.status === 'flying').length
    const flying = drones.value.filter((item) => item.status === 'flying').length
    const illegal = drones.value.filter((item) => item.isIllegal).length

    return [
      { label: '总设备数', value: total, icon: 'mdi:quadcopter', color: '#00d4ff' },
      { label: '在线设备', value: online, icon: 'mdi:wifi', color: '#00ff88' },
      { label: '执行任务', value: flying, icon: 'mdi:flight', color: '#ffaa00' },
      { label: '未授权无人机告警', value: illegal, icon: 'mdi:alert', color: '#ff4d4f' }
    ]
  })

  const missionStats = computed<MissionStat[]>(() => {
    const running = missions.value.filter((item) => item.status === 'running').length
    const completed = missions.value.filter((item) => item.status === 'completed').length
    const pending = missions.value.filter((item) => item.status === 'pending').length
    const cancelled = missions.value.filter((item) => item.status === 'cancelled').length
    const total = missions.value.length

    return [
      { label: '执行中', value: running, percent: calcPercent(running, total), color: '#ffaa00' },
      { label: '已完成', value: completed, percent: calcPercent(completed, total), color: '#00ff88' },
      { label: '待执行', value: pending, percent: calcPercent(pending, total), color: '#00d4ff' },
      { label: '已取消', value: cancelled, percent: calcPercent(cancelled, total), color: '#ff4d4f' }
    ]
  })

  const ensureMotionProfile = (drone: DemoDrone) => {
    const exists = motionProfileMap.get(drone.id)
    if (exists) {
      return exists
    }
    const profile = buildMotionProfile(drone)
    motionProfileMap.set(drone.id, profile)
    return profile
  }

  const reseedMotionProfiles = () => {
    const activeDroneIds = new Set(drones.value.map((drone) => drone.id))
    drones.value.forEach((drone) => ensureMotionProfile(drone))
    Array.from(motionProfileMap.keys()).forEach((droneId) => {
      if (!activeDroneIds.has(droneId)) {
        motionProfileMap.delete(droneId)
      }
    })
  }

  const maybeSwitchDroneStatus = (drone: DemoDrone) => {
    if (drone.isIllegal) {
      drone.status = 'flying'
      drone.statusText = getDroneStatusText(drone)
      return
    }

    const roll = Math.random()

    if (drone.status === 'offline') {
      if (roll < 0.32) {
        drone.status = 'online'
        drone.alt = Math.max(drone.alt, 120)
        drone.battery = drone.battery ?? Math.floor(68 + Math.random() * 26)
      }
    } else if (drone.status === 'online') {
      if (roll < 0.24) {
        drone.status = 'flying'
        drone.alt = Math.max(drone.alt, 220)
      } else if (roll > 0.95) {
        drone.status = 'offline'
        drone.alt = 0
      }
    } else if (drone.status === 'flying') {
      if (roll < 0.18 || (drone.battery !== null && drone.battery <= 12)) {
        drone.status = 'online'
      }
    }

    drone.statusText = getDroneStatusText(drone)
  }

  const updateFlyingDroneTelemetry = (drone: DemoDrone, profile: DroneMotionProfile) => {
    profile.phase += profile.angularSpeed
    profile.altitudePhase += profile.angularSpeed * 0.7
    const driftLng = Math.cos(profile.phase) * profile.radiusLng
    const driftLat = Math.sin(profile.phase) * profile.radiusLat
    const altWave = Math.sin(profile.altitudePhase) * 45
    const randomAltJitter = (Math.random() - 0.5) * 12

    drone.lng = Number((profile.centerLng + driftLng).toFixed(6))
    drone.lat = Number((profile.centerLat + driftLat).toFixed(6))
    drone.alt = Math.round(clampNumber(profile.baseAlt + altWave + randomAltJitter, 180, 1200))

    if (drone.battery !== null) {
      const drain = Math.random() < 0.7 ? 1 : 2
      drone.battery = clampNumber(drone.battery - drain, 5, 100)
    }
  }

  const updateIdleDroneTelemetry = (drone: DemoDrone, profile: DroneMotionProfile) => {
    profile.phase += profile.angularSpeed * 0.35
    const idleLng = profile.centerLng + Math.cos(profile.phase) * profile.radiusLng * 0.1
    const idleLat = profile.centerLat + Math.sin(profile.phase) * profile.radiusLat * 0.1
    drone.lng = Number(idleLng.toFixed(6))
    drone.lat = Number(idleLat.toFixed(6))

    if (drone.status === 'online') {
      drone.alt = Math.round(clampNumber(profile.baseAlt * 0.5 + Math.sin(profile.phase * 0.8) * 12, 80, 260))
      if (drone.battery !== null) {
        const drift = Math.random() < 0.6 ? 1 : -1
        drone.battery = clampNumber(drone.battery + drift, 10, 100)
      } else {
        drone.battery = Math.floor(58 + Math.random() * 30)
      }
    } else {
      drone.alt = 0
      if (!drone.isIllegal) {
        drone.battery = null
      }
    }
  }

  const updateDroneTelemetry = () => {
    drones.value.forEach((drone) => {
      const profile = ensureMotionProfile(drone)
      if (drone.status === 'flying') {
        updateFlyingDroneTelemetry(drone, profile)
      } else {
        updateIdleDroneTelemetry(drone, profile)
      }
      drone.statusText = getDroneStatusText(drone)
    })
  }

  const promotePendingMission = () => {
    const runningCount = missions.value.filter((item) => item.status === 'running').length
    if (runningCount >= 3 || Math.random() < 0.65) {
      return
    }

    const pendingMission = missions.value.find((item) => item.status === 'pending')
    if (!pendingMission) {
      return
    }

    pendingMission.status = 'running'
    pendingMission.statusText = '执行中'
    pendingMission.progress = Math.floor(Math.random() * 15) + 5
    pendingMission.time = formatMissionTime(new Date())
  }

  const updateMissionProgress = () => {
    missions.value.forEach((mission) => {
      if (mission.status !== 'running' || mission.progress === null || mission.progress >= 100) {
        return
      }

      mission.progress = Math.min(100, mission.progress + Math.floor(Math.random() * 3) + 1)
      if (mission.progress >= 100) {
        mission.status = 'completed'
        mission.statusText = '已完成'
      }
    })

    promotePendingMission()
  }

  const rescheduleMissionPool = () => {
    missions.value.forEach((mission) => {
      const roll = Math.random()

      if (mission.status === 'completed' && roll < 0.28) {
        mission.status = 'pending'
        mission.statusText = '待执行'
        mission.progress = null
        mission.time = formatMissionTime(new Date(Date.now() + (Math.floor(Math.random() * 90) + 20) * 60000))
        return
      }

      if (mission.status === 'cancelled' && roll < 0.45) {
        mission.status = 'pending'
        mission.statusText = '待执行'
        mission.progress = null
        mission.time = formatMissionTime(new Date(Date.now() + (Math.floor(Math.random() * 60) + 10) * 60000))
        return
      }

      if (mission.status === 'running' && roll > 0.975) {
        mission.status = 'cancelled'
        mission.statusText = '已取消'
        mission.progress = null
      }
    })

    promotePendingMission()
  }

  onMounted(() => {
    reseedMotionProfiles()
    updateDroneTelemetry()
    missionTimer = window.setInterval(updateMissionProgress, 3000)
    droneTelemetryTimer = window.setInterval(updateDroneTelemetry, 1000)
    droneStatusTimer = window.setInterval(() => {
      drones.value.forEach((drone) => maybeSwitchDroneStatus(drone))
      updateDroneTelemetry()
    }, 12000)
    missionRescheduleTimer = window.setInterval(rescheduleMissionPool, 9000)
  })

  onUnmounted(() => {
    if (missionTimer) {
      clearInterval(missionTimer)
      missionTimer = null
    }
    if (droneTelemetryTimer) {
      clearInterval(droneTelemetryTimer)
      droneTelemetryTimer = null
    }
    if (droneStatusTimer) {
      clearInterval(droneStatusTimer)
      droneStatusTimer = null
    }
    if (missionRescheduleTimer) {
      clearInterval(missionRescheduleTimer)
      missionRescheduleTimer = null
    }
  })

  return {
    drones,
    missions,
    droneStats,
    missionStats
  }
}
