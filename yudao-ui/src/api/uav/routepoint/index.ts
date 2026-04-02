import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 航线点位信息 */
export interface RoutePoint {
          id: number; // 主键
          routeId?: number; // 航线
          seqNo?: number; // 点位序号
          lng?: number; // 经度
          lat?: number; // 纬度
          alt: number; // 高度
          speedMps: number; // 速度
          actionType: string; // 动作类型
          actionParam: string; // 动作参数
          remark: string; // 备注
  }

// 航线点位 API
export const RoutePointApi = {
  // 查询航线点位分页
  getRoutePointPage: async (params: any) => {
    return await request.get({ url: `/uav/route-point/page`, params })
  },

  // 查询航线点位详情
  getRoutePoint: async (id: number) => {
    return await request.get({ url: `/uav/route-point/get?id=` + id })
  },

  // 新增航线点位
  createRoutePoint: async (data: RoutePoint) => {
    return await request.post({ url: `/uav/route-point/create`, data })
  },

  // 修改航线点位
  updateRoutePoint: async (data: RoutePoint) => {
    return await request.put({ url: `/uav/route-point/update`, data })
  },

  // 删除航线点位
  deleteRoutePoint: async (id: number) => {
    return await request.delete({ url: `/uav/route-point/delete?id=` + id })
  },

  /** 批量删除航线点位 */
  deleteRoutePointList: async (ids: number[]) => {
    return await request.delete({ url: `/uav/route-point/delete-list?ids=${ids.join(',')}` })
  },

  // 导出航线点位 Excel
  exportRoutePoint: async (params) => {
    return await request.download({ url: `/uav/route-point/export-excel`, params })
  }
}