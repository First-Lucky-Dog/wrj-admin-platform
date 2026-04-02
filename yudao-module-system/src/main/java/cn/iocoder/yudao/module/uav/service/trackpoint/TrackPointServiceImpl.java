package cn.iocoder.yudao.module.uav.service.trackpoint;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.uav.controller.admin.trackpoint.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.trackpoint.TrackPointDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.uav.dal.mysql.trackpoint.TrackPointMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;

/**
 * 飞行轨迹点 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class TrackPointServiceImpl implements TrackPointService {

    @Resource
    private TrackPointMapper trackPointMapper;

    @Override
    public Long createTrackPoint(TrackPointSaveReqVO createReqVO) {
        // 插入
        TrackPointDO trackPoint = BeanUtils.toBean(createReqVO, TrackPointDO.class);
        trackPointMapper.insert(trackPoint);

        // 返回
        return trackPoint.getId();
    }

    @Override
    public void updateTrackPoint(TrackPointSaveReqVO updateReqVO) {
        // 校验存在
        validateTrackPointExists(updateReqVO.getId());
        // 更新
        TrackPointDO updateObj = BeanUtils.toBean(updateReqVO, TrackPointDO.class);
        trackPointMapper.updateById(updateObj);
    }

    @Override
    public void deleteTrackPoint(Long id) {
        // 校验存在
        validateTrackPointExists(id);
        // 删除
        trackPointMapper.deleteById(id);
    }

    @Override
        public void deleteTrackPointListByIds(List<Long> ids) {
        // 删除
        trackPointMapper.deleteByIds(ids);
        }


    private void validateTrackPointExists(Long id) {
        if (trackPointMapper.selectById(id) == null) {
            throw exception(new ErrorCode(401, "轨迹点不存在"));
        }
    }

    @Override
    public TrackPointDO getTrackPoint(Long id) {
        return trackPointMapper.selectById(id);
    }

    @Override
    public PageResult<TrackPointDO> getTrackPointPage(TrackPointPageReqVO pageReqVO) {
        return trackPointMapper.selectPage(pageReqVO);
    }

}