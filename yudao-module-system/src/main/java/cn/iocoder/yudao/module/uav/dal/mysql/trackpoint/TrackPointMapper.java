package cn.iocoder.yudao.module.uav.dal.mysql.trackpoint;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.uav.dal.dataobject.trackpoint.TrackPointDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.uav.controller.admin.trackpoint.vo.*;

/**
 * 飞行轨迹点 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface TrackPointMapper extends BaseMapperX<TrackPointDO> {

    default PageResult<TrackPointDO> selectPage(TrackPointPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TrackPointDO>()
                .eqIfPresent(TrackPointDO::getMissionId, reqVO.getMissionId())
                .eqIfPresent(TrackPointDO::getDeviceId, reqVO.getDeviceId())
                .betweenIfPresent(TrackPointDO::getTrackTime, reqVO.getTrackTime())
                .betweenIfPresent(TrackPointDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(TrackPointDO::getId));
    }

}