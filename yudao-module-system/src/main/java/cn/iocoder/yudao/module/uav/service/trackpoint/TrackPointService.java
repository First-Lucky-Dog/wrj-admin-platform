package cn.iocoder.yudao.module.uav.service.trackpoint;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.uav.controller.admin.trackpoint.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.trackpoint.TrackPointDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 飞行轨迹点 Service 接口
 *
 * @author 芋道源码
 */
public interface TrackPointService {

    /**
     * 创建飞行轨迹点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTrackPoint(@Valid TrackPointSaveReqVO createReqVO);

    /**
     * 更新飞行轨迹点
     *
     * @param updateReqVO 更新信息
     */
    void updateTrackPoint(@Valid TrackPointSaveReqVO updateReqVO);

    /**
     * 删除飞行轨迹点
     *
     * @param id 编号
     */
    void deleteTrackPoint(Long id);

    /**
    * 批量删除飞行轨迹点
    *
    * @param ids 编号
    */
    void deleteTrackPointListByIds(List<Long> ids);

    /**
     * 获得飞行轨迹点
     *
     * @param id 编号
     * @return 飞行轨迹点
     */
    TrackPointDO getTrackPoint(Long id);

    /**
     * 获得飞行轨迹点分页
     *
     * @param pageReqVO 分页查询
     * @return 飞行轨迹点分页
     */
    PageResult<TrackPointDO> getTrackPointPage(TrackPointPageReqVO pageReqVO);

}