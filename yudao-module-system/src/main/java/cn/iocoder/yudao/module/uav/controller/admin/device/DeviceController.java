package cn.iocoder.yudao.module.uav.controller.admin.device;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.uav.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.device.DeviceDO;
import cn.iocoder.yudao.module.uav.service.device.DeviceService;

@Tag(name = "管理后台 - 无人机设备")
@RestController
@RequestMapping("/uav/device")
@Validated
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建无人机设备")
    @PreAuthorize("@ss.hasPermission('uav:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody DeviceSaveReqVO createReqVO) {
        return success(deviceService.createDevice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新无人机设备")
    @PreAuthorize("@ss.hasPermission('uav:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody DeviceSaveReqVO updateReqVO) {
        deviceService.updateDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除无人机设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('uav:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除无人机设备")
                @PreAuthorize("@ss.hasPermission('uav:device:delete')")
    public CommonResult<Boolean> deleteDeviceList(@RequestParam("ids") List<Long> ids) {
        deviceService.deleteDeviceListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得无人机设备")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('uav:device:query')")
    public CommonResult<DeviceRespVO> getDevice(@RequestParam("id") Long id) {
        DeviceDO device = deviceService.getDevice(id);
        return success(BeanUtils.toBean(device, DeviceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得无人机设备分页")
    @PreAuthorize("@ss.hasPermission('uav:device:query')")
    public CommonResult<PageResult<DeviceRespVO>> getDevicePage(@Valid DevicePageReqVO pageReqVO) {
        PageResult<DeviceDO> pageResult = deviceService.getDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DeviceRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出无人机设备 Excel")
    @PreAuthorize("@ss.hasPermission('uav:device:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceExcel(@Valid DevicePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DeviceDO> list = deviceService.getDevicePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "无人机设备.xls", "数据", DeviceRespVO.class,
                        BeanUtils.toBean(list, DeviceRespVO.class));
    }

    @GetMapping("/select-data")
    @Operation(summary = "获得无人机设备下拉数据")
    @PreAuthorize("@ss.hasPermission('uav:device:query')")
    public CommonResult<List<DeviceRespVO>> getDeviceSelectList() {
        List<DeviceDO> list = deviceService.getDeviceSelect();
        return success(BeanUtils.toBean(list, DeviceRespVO.class));
    }

}