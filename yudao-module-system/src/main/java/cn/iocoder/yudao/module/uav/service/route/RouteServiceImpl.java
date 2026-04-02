package cn.iocoder.yudao.module.uav.service.route;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.uav.controller.admin.route.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.route.RouteDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.uav.dal.mysql.route.RouteMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;

/**
 * 航线模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class RouteServiceImpl implements RouteService {

    @Resource
    private RouteMapper routeMapper;

    @Override
    public Long createRoute(RouteSaveReqVO createReqVO) {
        // 插入
        RouteDO route = BeanUtils.toBean(createReqVO, RouteDO.class);
        routeMapper.insert(route);

        // 返回
        return route.getId();
    }

    @Override
    public void updateRoute(RouteSaveReqVO updateReqVO) {
        // 校验存在
        validateRouteExists(updateReqVO.getId());
        // 更新
        RouteDO updateObj = BeanUtils.toBean(updateReqVO, RouteDO.class);
        routeMapper.updateById(updateObj);
    }

    @Override
    public void deleteRoute(Long id) {
        // 校验存在
        validateRouteExists(id);
        // 删除
        routeMapper.deleteById(id);
    }

    @Override
        public void deleteRouteListByIds(List<Long> ids) {
        // 删除
        routeMapper.deleteByIds(ids);
        }


    private void validateRouteExists(Long id) {
        if (routeMapper.selectById(id) == null) {
            throw exception(new ErrorCode(401, "航线模板不存在"));
        }
    }

    @Override
    public RouteDO getRoute(Long id) {
        return routeMapper.selectById(id);
    }

    @Override
    public PageResult<RouteDO> getRoutePage(RoutePageReqVO pageReqVO) {
        return routeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<RouteDO> getRouteSelect() {
        return routeMapper.selectList(new LambdaQueryWrapperX<RouteDO>()
                .select(RouteDO::getId, RouteDO::getRouteName)
                .eq(RouteDO::getDeleted, false)
                .eq(RouteDO::getStatus, 1)
        );
    }
}