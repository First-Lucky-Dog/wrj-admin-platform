import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 飞行轨迹点信息 */
export interface TrackPoint {
          id: number; // 主键
          missionId: number; // 任务
          deviceId?: number; // 设备
          trackTime?: string | Dayjs; // 轨迹时间
          lng?: number; // 经度
          lat?: number; // 纬度
          alt: number; // 高度
          speedMps: number; // 速度
          batteryLevel: number; // 电量
          heading: number; // 航向角
          extraJson: string; // 扩展数据
  }

// 飞行轨迹点 API
export const TrackPointApi = {
  // 查询飞行轨迹点分页
  getTrackPointPage: async (params: any) => {
    return await request.get({ url: `/uav/track-point/page`, params })
  },

  // 查询飞行轨迹点详情
  getTrackPoint: async (id: number) => {
    return await request.get({ url: `/uav/track-point/get?id=` + id })
  },

  // 新增飞行轨迹点
  createTrackPoint: async (data: TrackPoint) => {
    return await request.post({ url: `/uav/track-point/create`, data })
  },

  // 修改飞行轨迹点
  updateTrackPoint: async (data: TrackPoint) => {
    return await request.put({ url: `/uav/track-point/update`, data })
  },

  // 删除飞行轨迹点
  deleteTrackPoint: async (id: number) => {
    return await request.delete({ url: `/uav/track-point/delete?id=` + id })
  },

  /** 批量删除飞行轨迹点 */
  deleteTrackPointList: async (ids: number[]) => {
    return await request.delete({ url: `/uav/track-point/delete-list?ids=${ids.join(',')}` })
  },

  // 导出飞行轨迹点 Excel
  exportTrackPoint: async (params) => {
    return await request.download({ url: `/uav/track-point/export-excel`, params })
  }
}