import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 无人机设备信息 */
export interface Device {
          id: number; // 主键
          deviceCode?: string; // 设备编码
          deviceName?: string; // 设备名称
          model: string; // 型号
          onlineStatus?: number; // 在线状态
          flightStatus?: number; // 飞行状态
          batteryLevel: number; // 电量
          lng: number; // 经度
          lat: number; // 纬度
          alt: number; // 高度
          lastHeartbeatTime: string | Dayjs; // 最后心跳时间
          remark: string; // 备注
  }

// 无人机设备 API
export const DeviceApi = {
  // 查询无人机设备分页
  getDevicePage: async (params: any) => {
    return await request.get({ url: `/uav/device/page`, params })
  },

  // 查询无人机设备详情
  getDevice: async (id: number) => {
    return await request.get({ url: `/uav/device/get?id=` + id })
  },

  // 新增无人机设备
  createDevice: async (data: Device) => {
    return await request.post({ url: `/uav/device/create`, data })
  },

  // 修改无人机设备
  updateDevice: async (data: Device) => {
    return await request.put({ url: `/uav/device/update`, data })
  },

  // 删除无人机设备
  deleteDevice: async (id: number) => {
    return await request.delete({ url: `/uav/device/delete?id=` + id })
  },

  /** 批量删除无人机设备 */
  deleteDeviceList: async (ids: number[]) => {
    return await request.delete({ url: `/uav/device/delete-list?ids=${ids.join(',')}` })
  },

  // 导出无人机设备 Excel
  exportDevice: async (params) => {
    return await request.download({ url: `/uav/device/export-excel`, params })
  },

  // 获取下拉数据
  getDeviceSelect: async () => {
    return await request.get({ url: `/uav/device/select-data` })
  }

}
