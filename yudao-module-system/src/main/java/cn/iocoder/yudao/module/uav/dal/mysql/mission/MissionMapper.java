package cn.iocoder.yudao.module.uav.dal.mysql.mission;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.uav.dal.dataobject.mission.MissionDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.uav.controller.admin.mission.vo.*;

/**
 * 飞行任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MissionMapper extends BaseMapperX<MissionDO> {

    default PageResult<MissionDO> selectPage(MissionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MissionDO>()
                .eqIfPresent(MissionDO::getMissionNo, reqVO.getMissionNo())
                .eqIfPresent(MissionDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(MissionDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(MissionDO::getMissionType, reqVO.getMissionType())
                .eqIfPresent(MissionDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MissionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MissionDO::getId));
    }

}