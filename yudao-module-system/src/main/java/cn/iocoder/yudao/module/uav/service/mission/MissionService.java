package cn.iocoder.yudao.module.uav.service.mission;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.uav.controller.admin.mission.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.mission.MissionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 飞行任务 Service 接口
 *
 * @author 芋道源码
 */
public interface MissionService {

    /**
     * 创建飞行任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMission(@Valid MissionSaveReqVO createReqVO);

    /**
     * 更新飞行任务
     *
     * @param updateReqVO 更新信息
     */
    void updateMission(@Valid MissionSaveReqVO updateReqVO);

    /**
     * 删除飞行任务
     *
     * @param id 编号
     */
    void deleteMission(Long id);

    /**
    * 批量删除飞行任务
    *
    * @param ids 编号
    */
    void deleteMissionListByIds(List<Long> ids);

    /**
     * 获得飞行任务
     *
     * @param id 编号
     * @return 飞行任务
     */
    MissionDO getMission(Long id);

    /**
     * 获得飞行任务分页
     *
     * @param pageReqVO 分页查询
     * @return 飞行任务分页
     */
    PageResult<MissionDO> getMissionPage(MissionPageReqVO pageReqVO);

    List<MissionDO> getMissionSelect();

}