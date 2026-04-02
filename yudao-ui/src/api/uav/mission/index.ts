import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 飞行任务信息 */
export interface Mission {
          id: number; // 主键
          missionNo?: string; // 任务编号
          deviceId?: number; // 设备
          routeId: number; // 航线
          missionType?: number; // 任务类型
          status?: number; // 状态
          planStartTime: string | Dayjs; // 计划开始时间
          startTime: string | Dayjs; // 实际开始时间
          endTime: string | Dayjs; // 实际结束时间
          actualDistanceM: number; // 实际里程
          actualDurationS: number; // 实际时长
          failReason: string; // 失败原因
          remark: string; // 备注
  }

// 飞行任务 API
export const MissionApi = {
  // 查询飞行任务分页
  getMissionPage: async (params: any) => {
    return await request.get({ url: `/uav/mission/page`, params })
  },

  // 查询飞行任务详情
  getMission: async (id: number) => {
    return await request.get({ url: `/uav/mission/get?id=` + id })
  },

  // 新增飞行任务
  createMission: async (data: Mission) => {
    return await request.post({ url: `/uav/mission/create`, data })
  },

  // 修改飞行任务
  updateMission: async (data: Mission) => {
    return await request.put({ url: `/uav/mission/update`, data })
  },

  // 删除飞行任务
  deleteMission: async (id: number) => {
    return await request.delete({ url: `/uav/mission/delete?id=` + id })
  },

  /** 批量删除飞行任务 */
  deleteMissionList: async (ids: number[]) => {
    return await request.delete({ url: `/uav/mission/delete-list?ids=${ids.join(',')}` })
  },

  // 导出飞行任务 Excel
  exportMission: async (params) => {
    return await request.download({ url: `/uav/mission/export-excel`, params })
  },

  // 获取下拉数据
  getMissionSelect: async () => {
    return await request.get({ url: `/uav/mission/select-data` })
  },

}
