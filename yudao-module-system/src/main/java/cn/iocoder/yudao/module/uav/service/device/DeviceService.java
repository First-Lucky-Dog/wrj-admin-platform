package cn.iocoder.yudao.module.uav.service.device;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.uav.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.device.DeviceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 无人机设备 Service 接口
 *
 * @author 芋道源码
 */
public interface DeviceService {

    /**
     * 创建无人机设备
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDevice(@Valid DeviceSaveReqVO createReqVO);

    /**
     * 更新无人机设备
     *
     * @param updateReqVO 更新信息
     */
    void updateDevice(@Valid DeviceSaveReqVO updateReqVO);

    /**
     * 删除无人机设备
     *
     * @param id 编号
     */
    void deleteDevice(Long id);

    /**
    * 批量删除无人机设备
    *
    * @param ids 编号
    */
    void deleteDeviceListByIds(List<Long> ids);

    /**
     * 获得无人机设备
     *
     * @param id 编号
     * @return 无人机设备
     */
    DeviceDO getDevice(Long id);

    /**
     * 获得无人机设备分页
     *
     * @param pageReqVO 分页查询
     * @return 无人机设备分页
     */
    PageResult<DeviceDO> getDevicePage(DevicePageReqVO pageReqVO);

    List<DeviceDO> getDeviceSelect();

}