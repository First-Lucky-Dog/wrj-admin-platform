import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 控制指令日志信息 */
export interface CommandLog {
          id: number; // 主键
          deviceId?: number; // 设备
          missionId?: number; // 任务
          commandType?: string; // 指令类型
          commandPayload: string; // 指令参数
          sendStatus?: number; // 下发状态
          ackStatus?: number; // 回执状态
          ackMessage: string; // 回执信息
          operatorId: number; // 操作人ID
          operatorName: string; // 操作人
          sendTime: string | Dayjs; // 下发时间
          ackTime: string | Dayjs; // 回执时间
          remark: string; // 备注
  }

// 控制指令日志 API
export const CommandLogApi = {
  // 查询控制指令日志分页
  getCommandLogPage: async (params: any) => {
    return await request.get({ url: `/uav/command-log/page`, params })
  },

  // 查询控制指令日志详情
  getCommandLog: async (id: number) => {
    return await request.get({ url: `/uav/command-log/get?id=` + id })
  },

  // 新增控制指令日志
  createCommandLog: async (data: CommandLog) => {
    return await request.post({ url: `/uav/command-log/create`, data })
  },

  // 修改控制指令日志
  updateCommandLog: async (data: CommandLog) => {
    return await request.put({ url: `/uav/command-log/update`, data })
  },

  // 删除控制指令日志
  deleteCommandLog: async (id: number) => {
    return await request.delete({ url: `/uav/command-log/delete?id=` + id })
  },

  /** 批量删除控制指令日志 */
  deleteCommandLogList: async (ids: number[]) => {
    return await request.delete({ url: `/uav/command-log/delete-list?ids=${ids.join(',')}` })
  },

  // 导出控制指令日志 Excel
  exportCommandLog: async (params) => {
    return await request.download({ url: `/uav/command-log/export-excel`, params })
  }
}