package cn.iocoder.yudao.module.uav.dal.mysql.device;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.uav.dal.dataobject.device.DeviceDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.uav.controller.admin.device.vo.*;

/**
 * 无人机设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DeviceMapper extends BaseMapperX<DeviceDO> {

    default PageResult<DeviceDO> selectPage(DevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeviceDO>()
                .eqIfPresent(DeviceDO::getDeviceCode, reqVO.getDeviceCode())
                .likeIfPresent(DeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(DeviceDO::getModel, reqVO.getModel())
                .eqIfPresent(DeviceDO::getOnlineStatus, reqVO.getOnlineStatus())
                .eqIfPresent(DeviceDO::getFlightStatus, reqVO.getFlightStatus())
                .betweenIfPresent(DeviceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DeviceDO::getId));
    }

}