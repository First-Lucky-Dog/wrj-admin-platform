package cn.iocoder.yudao.module.uav.dal.mysql.routepoint;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.uav.dal.dataobject.routepoint.RoutePointDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.uav.controller.admin.routepoint.vo.*;

/**
 * 航线点位 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface RoutePointMapper extends BaseMapperX<RoutePointDO> {

    default PageResult<RoutePointDO> selectPage(RoutePointPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoutePointDO>()
                .eqIfPresent(RoutePointDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(RoutePointDO::getSeqNo, reqVO.getSeqNo())
                .eqIfPresent(RoutePointDO::getActionType, reqVO.getActionType())
                .betweenIfPresent(RoutePointDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RoutePointDO::getId));
    }

}