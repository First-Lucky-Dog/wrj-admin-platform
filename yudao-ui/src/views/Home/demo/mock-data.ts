export type DroneCategory = 'logistics' | 'commercial' | 'public' | 'traffic'
export type DroneStatus = 'flying' | 'online' | 'offline'

export interface DemoDrone {
  id: number
  name: string
  model: string
  lng: number
  lat: number
  alt: number
  status: DroneStatus
  statusText: string
  battery: number | null
  category: DroneCategory
  isIllegal: boolean
}

export type MissionStatus = 'running' | 'pending' | 'completed' | 'cancelled'

export interface DemoMission {
  id: number
  name: string
  deviceId: number
  deviceName: string
  status: MissionStatus
  statusText: string
  time: string
  progress: number | null
}

export const ALL_DRONE_CATEGORIES: DroneCategory[] = [
  'logistics',
  'commercial',
  'public',
  'traffic'
]

export const DEMO_DRONES_SEED: DemoDrone[] = [
  { id: 1, name: 'UAV-001', model: 'DJI Mavic 3', lng: 120.153, lat: 30.287, alt: 500, status: 'flying', statusText: '飞行中', battery: 78, category: 'logistics', isIllegal: false },
  { id: 2, name: 'UAV-002', model: 'DJI Phantom 4', lng: 121.550, lat: 29.868, alt: 300, status: 'online', statusText: '在线', battery: 95, category: 'commercial', isIllegal: false },
  { id: 3, name: 'UAV-003', model: 'DJI Mavic 3', lng: 120.699, lat: 27.994, alt: 600, status: 'flying', statusText: '飞行中', battery: 62, category: 'public', isIllegal: false },
  { id: 4, name: 'UAV-004', model: 'DJI Inspire 2', lng: 120.755, lat: 30.753, alt: 400, status: 'online', statusText: '在线', battery: 88, category: 'traffic', isIllegal: false },
  { id: 5, name: 'UAV-005', model: 'DJI Mavic 3', lng: 120.093, lat: 30.894, alt: 0, status: 'offline', statusText: '离线', battery: null, category: 'logistics', isIllegal: false },
  { id: 6, name: 'UAV-006', model: 'DJI Phantom 4', lng: 120.582, lat: 29.997, alt: 350, status: 'online', statusText: '在线', battery: 100, category: 'commercial', isIllegal: false },
  { id: 7, name: 'UAV-007', model: 'DJI Mavic 3', lng: 119.649, lat: 29.089, alt: 550, status: 'flying', statusText: '飞行中', battery: 45, category: 'public', isIllegal: false },
  { id: 8, name: 'UAV-008', model: 'DJI Inspire 2', lng: 121.429, lat: 28.661, alt: 450, status: 'online', statusText: '在线', battery: 72, category: 'traffic', isIllegal: false },
  { id: 9, name: 'UAV-X01', model: '未知型号', lng: 120.210, lat: 30.302, alt: 800, status: 'flying', statusText: '未授权无人机', battery: null, category: 'commercial', isIllegal: true },
  { id: 10, name: 'UAV-X02', model: '未知型号', lng: 122.207, lat: 29.985, alt: 650, status: 'flying', statusText: '未授权无人机', battery: null, category: 'logistics', isIllegal: true },
  { id: 11, name: 'UAV-X03', model: '未知型号', lng: 118.873, lat: 28.942, alt: 720, status: 'flying', statusText: '未授权无人机', battery: null, category: 'commercial', isIllegal: true }
]

export const DEMO_MISSIONS_SEED: DemoMission[] = [
  { id: 1, name: '杭州湾巡检任务', deviceId: 1, deviceName: 'UAV-001', status: 'running', statusText: '执行中', time: '10:30', progress: 65 },
  { id: 2, name: '温州沿海物资投送', deviceId: 3, deviceName: 'UAV-003', status: 'running', statusText: '执行中', time: '11:15', progress: 42 },
  { id: 3, name: '金华山区环境监测', deviceId: 7, deviceName: 'UAV-007', status: 'running', statusText: '执行中', time: '12:00', progress: 28 },
  { id: 4, name: '宁波港区安全巡查', deviceId: 2, deviceName: 'UAV-002', status: 'pending', statusText: '待执行', time: '14:00', progress: null },
  { id: 5, name: '嘉兴城市应急响应', deviceId: 4, deviceName: 'UAV-004', status: 'pending', statusText: '待执行', time: '15:30', progress: null },
  { id: 6, name: '绍兴工业带数据采集', deviceId: 6, deviceName: 'UAV-006', status: 'completed', statusText: '已完成', time: '09:00', progress: 100 }
]
