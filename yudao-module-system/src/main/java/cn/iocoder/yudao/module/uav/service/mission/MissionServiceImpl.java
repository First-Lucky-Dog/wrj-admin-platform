package cn.iocoder.yudao.module.uav.service.mission;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.uav.controller.admin.mission.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.mission.MissionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.uav.dal.mysql.mission.MissionMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;

/**
 * 飞行任务 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MissionServiceImpl implements MissionService {

    @Resource
    private MissionMapper missionMapper;

    @Override
    public Long createMission(MissionSaveReqVO createReqVO) {
        // 插入
        MissionDO mission = BeanUtils.toBean(createReqVO, MissionDO.class);
        missionMapper.insert(mission);

        // 返回
        return mission.getId();
    }

    @Override
    public void updateMission(MissionSaveReqVO updateReqVO) {
        // 校验存在
        validateMissionExists(updateReqVO.getId());
        // 更新
        MissionDO updateObj = BeanUtils.toBean(updateReqVO, MissionDO.class);
        missionMapper.updateById(updateObj);
    }

    @Override
    public void deleteMission(Long id) {
        // 校验存在
        validateMissionExists(id);
        // 删除
        missionMapper.deleteById(id);
    }

    @Override
        public void deleteMissionListByIds(List<Long> ids) {
        // 删除
        missionMapper.deleteByIds(ids);
        }


    private void validateMissionExists(Long id) {
        if (missionMapper.selectById(id) == null) {
            throw exception(new ErrorCode(401, "飞行任务不存在"));
        }
    }

    @Override
    public MissionDO getMission(Long id) {
        return missionMapper.selectById(id);
    }

    @Override
    public PageResult<MissionDO> getMissionPage(MissionPageReqVO pageReqVO) {
        return missionMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MissionDO> getMissionSelect() {
        return missionMapper.selectList(new LambdaQueryWrapperX<MissionDO>()
                .select(MissionDO::getId, MissionDO::getMissionNo)
                .eq(MissionDO::getDeleted, false)
        );
    }
}