package cn.iocoder.yudao.module.uav.service.routepoint;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.uav.controller.admin.routepoint.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.routepoint.RoutePointDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 航线点位 Service 接口
 *
 * @author 芋道源码
 */
public interface RoutePointService {

    /**
     * 创建航线点位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRoutePoint(@Valid RoutePointSaveReqVO createReqVO);

    /**
     * 更新航线点位
     *
     * @param updateReqVO 更新信息
     */
    void updateRoutePoint(@Valid RoutePointSaveReqVO updateReqVO);

    /**
     * 删除航线点位
     *
     * @param id 编号
     */
    void deleteRoutePoint(Long id);

    /**
    * 批量删除航线点位
    *
    * @param ids 编号
    */
    void deleteRoutePointListByIds(List<Long> ids);

    /**
     * 获得航线点位
     *
     * @param id 编号
     * @return 航线点位
     */
    RoutePointDO getRoutePoint(Long id);

    /**
     * 获得航线点位分页
     *
     * @param pageReqVO 分页查询
     * @return 航线点位分页
     */
    PageResult<RoutePointDO> getRoutePointPage(RoutePointPageReqVO pageReqVO);

}