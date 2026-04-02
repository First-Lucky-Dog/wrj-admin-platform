package cn.iocoder.yudao.module.uav.controller.admin.mission;

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

import cn.iocoder.yudao.module.uav.controller.admin.mission.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.mission.MissionDO;
import cn.iocoder.yudao.module.uav.service.mission.MissionService;

@Tag(name = "管理后台 - 飞行任务")
@RestController
@RequestMapping("/uav/mission")
@Validated
public class MissionController {

    @Resource
    private MissionService missionService;

    @PostMapping("/create")
    @Operation(summary = "创建飞行任务")
    @PreAuthorize("@ss.hasPermission('uav:mission:create')")
    public CommonResult<Long> createMission(@Valid @RequestBody MissionSaveReqVO createReqVO) {
        return success(missionService.createMission(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新飞行任务")
    @PreAuthorize("@ss.hasPermission('uav:mission:update')")
    public CommonResult<Boolean> updateMission(@Valid @RequestBody MissionSaveReqVO updateReqVO) {
        missionService.updateMission(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除飞行任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('uav:mission:delete')")
    public CommonResult<Boolean> deleteMission(@RequestParam("id") Long id) {
        missionService.deleteMission(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除飞行任务")
                @PreAuthorize("@ss.hasPermission('uav:mission:delete')")
    public CommonResult<Boolean> deleteMissionList(@RequestParam("ids") List<Long> ids) {
        missionService.deleteMissionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得飞行任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('uav:mission:query')")
    public CommonResult<MissionRespVO> getMission(@RequestParam("id") Long id) {
        MissionDO mission = missionService.getMission(id);
        return success(BeanUtils.toBean(mission, MissionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得飞行任务分页")
    @PreAuthorize("@ss.hasPermission('uav:mission:query')")
    public CommonResult<PageResult<MissionRespVO>> getMissionPage(@Valid MissionPageReqVO pageReqVO) {
        PageResult<MissionDO> pageResult = missionService.getMissionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MissionRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出飞行任务 Excel")
    @PreAuthorize("@ss.hasPermission('uav:mission:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMissionExcel(@Valid MissionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MissionDO> list = missionService.getMissionPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "飞行任务.xls", "数据", MissionRespVO.class,
                        BeanUtils.toBean(list, MissionRespVO.class));
    }

    @GetMapping("/select-data")
    @Operation(summary = "获得飞行任务下拉")
    @PreAuthorize("@ss.hasPermission('uav:mission:query')")
    public CommonResult<List<MissionRespVO>> getMissionSelect() {
        List<MissionDO> list = missionService.getMissionSelect();
        return success(BeanUtils.toBean(list, MissionRespVO.class));
    }

}