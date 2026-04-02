package cn.iocoder.yudao.module.uav.controller.admin.routepoint;

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

import cn.iocoder.yudao.module.uav.controller.admin.routepoint.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.routepoint.RoutePointDO;
import cn.iocoder.yudao.module.uav.service.routepoint.RoutePointService;

@Tag(name = "管理后台 - 航线点位")
@RestController
@RequestMapping("/uav/route-point")
@Validated
public class RoutePointController {

    @Resource
    private RoutePointService routePointService;

    @PostMapping("/create")
    @Operation(summary = "创建航线点位")
    @PreAuthorize("@ss.hasPermission('uav:route-point:create')")
    public CommonResult<Long> createRoutePoint(@Valid @RequestBody RoutePointSaveReqVO createReqVO) {
        return success(routePointService.createRoutePoint(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新航线点位")
    @PreAuthorize("@ss.hasPermission('uav:route-point:update')")
    public CommonResult<Boolean> updateRoutePoint(@Valid @RequestBody RoutePointSaveReqVO updateReqVO) {
        routePointService.updateRoutePoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除航线点位")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('uav:route-point:delete')")
    public CommonResult<Boolean> deleteRoutePoint(@RequestParam("id") Long id) {
        routePointService.deleteRoutePoint(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除航线点位")
                @PreAuthorize("@ss.hasPermission('uav:route-point:delete')")
    public CommonResult<Boolean> deleteRoutePointList(@RequestParam("ids") List<Long> ids) {
        routePointService.deleteRoutePointListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得航线点位")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('uav:route-point:query')")
    public CommonResult<RoutePointRespVO> getRoutePoint(@RequestParam("id") Long id) {
        RoutePointDO routePoint = routePointService.getRoutePoint(id);
        return success(BeanUtils.toBean(routePoint, RoutePointRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得航线点位分页")
    @PreAuthorize("@ss.hasPermission('uav:route-point:query')")
    public CommonResult<PageResult<RoutePointRespVO>> getRoutePointPage(@Valid RoutePointPageReqVO pageReqVO) {
        PageResult<RoutePointDO> pageResult = routePointService.getRoutePointPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RoutePointRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出航线点位 Excel")
    @PreAuthorize("@ss.hasPermission('uav:route-point:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRoutePointExcel(@Valid RoutePointPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RoutePointDO> list = routePointService.getRoutePointPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "航线点位.xls", "数据", RoutePointRespVO.class,
                        BeanUtils.toBean(list, RoutePointRespVO.class));
    }

}