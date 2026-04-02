package cn.iocoder.yudao.module.uav.service.routepoint;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.uav.controller.admin.routepoint.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.routepoint.RoutePointDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.uav.dal.mysql.routepoint.RoutePointMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;

/**
 * 航线点位 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class RoutePointServiceImpl implements RoutePointService {

    @Resource
    private RoutePointMapper routePointMapper;

    @Override
    public Long createRoutePoint(RoutePointSaveReqVO createReqVO) {
        // 插入
        RoutePointDO routePoint = BeanUtils.toBean(createReqVO, RoutePointDO.class);
        routePointMapper.insert(routePoint);

        // 返回
        return routePoint.getId();
    }

    @Override
    public void updateRoutePoint(RoutePointSaveReqVO updateReqVO) {
        // 校验存在
        validateRoutePointExists(updateReqVO.getId());
        // 更新
        RoutePointDO updateObj = BeanUtils.toBean(updateReqVO, RoutePointDO.class);
        routePointMapper.updateById(updateObj);
    }

    @Override
    public void deleteRoutePoint(Long id) {
        // 校验存在
        validateRoutePointExists(id);
        // 删除
        routePointMapper.deleteById(id);
    }

    @Override
        public void deleteRoutePointListByIds(List<Long> ids) {
        // 删除
        routePointMapper.deleteByIds(ids);
        }


    private void validateRoutePointExists(Long id) {
        if (routePointMapper.selectById(id) == null) {
            throw exception(new ErrorCode(401, "航线点位不存在"));
        }
    }

    @Override
    public RoutePointDO getRoutePoint(Long id) {
        return routePointMapper.selectById(id);
    }

    @Override
    public PageResult<RoutePointDO> getRoutePointPage(RoutePointPageReqVO pageReqVO) {
        return routePointMapper.selectPage(pageReqVO);
    }

}