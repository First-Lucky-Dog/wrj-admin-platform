package cn.iocoder.yudao.module.uav.service.commandlog;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.uav.controller.admin.commandlog.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.commandlog.CommandLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 控制指令日志 Service 接口
 *
 * @author 芋道源码
 */
public interface CommandLogService {

    /**
     * 创建控制指令日志
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCommandLog(@Valid CommandLogSaveReqVO createReqVO);

    /**
     * 更新控制指令日志
     *
     * @param updateReqVO 更新信息
     */
    void updateCommandLog(@Valid CommandLogSaveReqVO updateReqVO);

    /**
     * 删除控制指令日志
     *
     * @param id 编号
     */
    void deleteCommandLog(Long id);

    /**
    * 批量删除控制指令日志
    *
    * @param ids 编号
    */
    void deleteCommandLogListByIds(List<Long> ids);

    /**
     * 获得控制指令日志
     *
     * @param id 编号
     * @return 控制指令日志
     */
    CommandLogDO getCommandLog(Long id);

    /**
     * 获得控制指令日志分页
     *
     * @param pageReqVO 分页查询
     * @return 控制指令日志分页
     */
    PageResult<CommandLogDO> getCommandLogPage(CommandLogPageReqVO pageReqVO);

}