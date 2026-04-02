package cn.iocoder.yudao.module.uav.controller.admin.trackpoint;

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

import cn.iocoder.yudao.module.uav.controller.admin.trackpoint.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.trackpoint.TrackPointDO;
import cn.iocoder.yudao.module.uav.service.trackpoint.TrackPointService;

@Tag(name = "管理后台 - 飞行轨迹点")
@RestController
@RequestMapping("/uav/track-point")
@Validated
public class TrackPointController {

    @Resource
    private TrackPointService trackPointService;

    @PostMapping("/create")
    @Operation(summary = "创建飞行轨迹点")
    @PreAuthorize("@ss.hasPermission('uav:track-point:create')")
    public CommonResult<Long> createTrackPoint(@Valid @RequestBody TrackPointSaveReqVO createReqVO) {
        return success(trackPointService.createTrackPoint(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新飞行轨迹点")
    @PreAuthorize("@ss.hasPermission('uav:track-point:update')")
    public CommonResult<Boolean> updateTrackPoint(@Valid @RequestBody TrackPointSaveReqVO updateReqVO) {
        trackPointService.updateTrackPoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除飞行轨迹点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('uav:track-point:delete')")
    public CommonResult<Boolean> deleteTrackPoint(@RequestParam("id") Long id) {
        trackPointService.deleteTrackPoint(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除飞行轨迹点")
                @PreAuthorize("@ss.hasPermission('uav:track-point:delete')")
    public CommonResult<Boolean> deleteTrackPointList(@RequestParam("ids") List<Long> ids) {
        trackPointService.deleteTrackPointListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得飞行轨迹点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('uav:track-point:query')")
    public CommonResult<TrackPointRespVO> getTrackPoint(@RequestParam("id") Long id) {
        TrackPointDO trackPoint = trackPointService.getTrackPoint(id);
        return success(BeanUtils.toBean(trackPoint, TrackPointRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得飞行轨迹点分页")
    @PreAuthorize("@ss.hasPermission('uav:track-point:query')")
    public CommonResult<PageResult<TrackPointRespVO>> getTrackPointPage(@Valid TrackPointPageReqVO pageReqVO) {
        PageResult<TrackPointDO> pageResult = trackPointService.getTrackPointPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TrackPointRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出飞行轨迹点 Excel")
    @PreAuthorize("@ss.hasPermission('uav:track-point:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTrackPointExcel(@Valid TrackPointPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TrackPointDO> list = trackPointService.getTrackPointPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "飞行轨迹点.xls", "数据", TrackPointRespVO.class,
                        BeanUtils.toBean(list, TrackPointRespVO.class));
    }

}