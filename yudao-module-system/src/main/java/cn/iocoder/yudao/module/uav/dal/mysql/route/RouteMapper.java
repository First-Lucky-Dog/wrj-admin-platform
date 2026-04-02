package cn.iocoder.yudao.module.uav.dal.mysql.route;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.uav.dal.dataobject.route.RouteDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.uav.controller.admin.route.vo.*;

/**
 * 航线模板 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface RouteMapper extends BaseMapperX<RouteDO> {

    default PageResult<RouteDO> selectPage(RoutePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RouteDO>()
                .likeIfPresent(RouteDO::getRouteName, reqVO.getRouteName())
                .eqIfPresent(RouteDO::getRouteType, reqVO.getRouteType())
                .eqIfPresent(RouteDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(RouteDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RouteDO::getId));
    }

}