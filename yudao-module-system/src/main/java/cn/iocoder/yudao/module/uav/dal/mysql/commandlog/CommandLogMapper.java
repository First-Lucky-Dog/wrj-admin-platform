package cn.iocoder.yudao.module.uav.dal.mysql.commandlog;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.uav.dal.dataobject.commandlog.CommandLogDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.uav.controller.admin.commandlog.vo.*;

/**
 * 控制指令日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CommandLogMapper extends BaseMapperX<CommandLogDO> {

    default PageResult<CommandLogDO> selectPage(CommandLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CommandLogDO>()
                .eqIfPresent(CommandLogDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(CommandLogDO::getMissionId, reqVO.getMissionId())
                .eqIfPresent(CommandLogDO::getCommandType, reqVO.getCommandType())
                .eqIfPresent(CommandLogDO::getSendStatus, reqVO.getSendStatus())
                .eqIfPresent(CommandLogDO::getAckStatus, reqVO.getAckStatus())
                .betweenIfPresent(CommandLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CommandLogDO::getId));
    }

}