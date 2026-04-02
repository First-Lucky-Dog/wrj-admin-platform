import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 航线模板信息 */
export interface Route {
          id: number; // 主键
          routeName?: string; // 航线名称
          routeType?: number; // 航线类型
          status?: number; // 状态
          estDistanceM: number; // 预计里程
          estDurationS: number; // 预计时长
          remark: string; // 备注
  }

// 航线模板 API
export const RouteApi = {
  // 查询航线模板分页
  getRoutePage: async (params: any) => {
    return await request.get({ url: `/uav/route/page`, params })
  },

  // 查询航线模板详情
  getRoute: async (id: number) => {
    return await request.get({ url: `/uav/route/get?id=` + id })
  },

  // 新增航线模板
  createRoute: async (data: Route) => {
    return await request.post({ url: `/uav/route/create`, data })
  },

  // 修改航线模板
  updateRoute: async (data: Route) => {
    return await request.put({ url: `/uav/route/update`, data })
  },

  // 删除航线模板
  deleteRoute: async (id: number) => {
    return await request.delete({ url: `/uav/route/delete?id=` + id })
  },

  /** 批量删除航线模板 */
  deleteRouteList: async (ids: number[]) => {
    return await request.delete({ url: `/uav/route/delete-list?ids=${ids.join(',')}` })
  },

  // 导出航线模板 Excel
  exportRoute: async (params) => {
    return await request.download({ url: `/uav/route/export-excel`, params })
  },

  // 获取航线模板下拉列表
  getRouteSelect: async () => {
    return await request.get({ url: `/uav/route/select-data` })
  }

}
