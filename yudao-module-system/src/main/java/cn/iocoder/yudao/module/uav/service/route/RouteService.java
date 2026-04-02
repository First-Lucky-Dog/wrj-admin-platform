package cn.iocoder.yudao.module.uav.service.route;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.uav.controller.admin.route.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.route.RouteDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 航线模板 Service 接口
 *
 * @author 芋道源码
 */
public interface RouteService {

    /**
     * 创建航线模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRoute(@Valid RouteSaveReqVO createReqVO);

    /**
     * 更新航线模板
     *
     * @param updateReqVO 更新信息
     */
    void updateRoute(@Valid RouteSaveReqVO updateReqVO);

    /**
     * 删除航线模板
     *
     * @param id 编号
     */
    void deleteRoute(Long id);

    /**
    * 批量删除航线模板
    *
    * @param ids 编号
    */
    void deleteRouteListByIds(List<Long> ids);

    /**
     * 获得航线模板
     *
     * @param id 编号
     * @return 航线模板
     */
    RouteDO getRoute(Long id);

    /**
     * 获得航线模板分页
     *
     * @param pageReqVO 分页查询
     * @return 航线模板分页
     */
    PageResult<RouteDO> getRoutePage(RoutePageReqVO pageReqVO);

    List<RouteDO> getRouteSelect();

}