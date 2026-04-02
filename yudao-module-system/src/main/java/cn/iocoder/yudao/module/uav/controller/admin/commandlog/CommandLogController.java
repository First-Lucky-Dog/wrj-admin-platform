package cn.iocoder.yudao.module.uav.controller.admin.commandlog;

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

import cn.iocoder.yudao.module.uav.controller.admin.commandlog.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.commandlog.CommandLogDO;
import cn.iocoder.yudao.module.uav.service.commandlog.CommandLogService;

@Tag(name = "管理后台 - 控制指令日志")
@RestController
@RequestMapping("/uav/command-log")
@Validated
public class CommandLogController {

    @Resource
    private CommandLogService commandLogService;

    @PostMapping("/create")
    @Operation(summary = "创建控制指令日志")
    @PreAuthorize("@ss.hasPermission('uav:command-log:create')")
    public CommonResult<Long> createCommandLog(@Valid @RequestBody CommandLogSaveReqVO createReqVO) {
        return success(commandLogService.createCommandLog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新控制指令日志")
    @PreAuthorize("@ss.hasPermission('uav:command-log:update')")
    public CommonResult<Boolean> updateCommandLog(@Valid @RequestBody CommandLogSaveReqVO updateReqVO) {
        commandLogService.updateCommandLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除控制指令日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('uav:command-log:delete')")
    public CommonResult<Boolean> deleteCommandLog(@RequestParam("id") Long id) {
        commandLogService.deleteCommandLog(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除控制指令日志")
                @PreAuthorize("@ss.hasPermission('uav:command-log:delete')")
    public CommonResult<Boolean> deleteCommandLogList(@RequestParam("ids") List<Long> ids) {
        commandLogService.deleteCommandLogListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得控制指令日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('uav:command-log:query')")
    public CommonResult<CommandLogRespVO> getCommandLog(@RequestParam("id") Long id) {
        CommandLogDO commandLog = commandLogService.getCommandLog(id);
        return success(BeanUtils.toBean(commandLog, CommandLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得控制指令日志分页")
    @PreAuthorize("@ss.hasPermission('uav:command-log:query')")
    public CommonResult<PageResult<CommandLogRespVO>> getCommandLogPage(@Valid CommandLogPageReqVO pageReqVO) {
        PageResult<CommandLogDO> pageResult = commandLogService.getCommandLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CommandLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出控制指令日志 Excel")
    @PreAuthorize("@ss.hasPermission('uav:command-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCommandLogExcel(@Valid CommandLogPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CommandLogDO> list = commandLogService.getCommandLogPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "控制指令日志.xls", "数据", CommandLogRespVO.class,
                        BeanUtils.toBean(list, CommandLogRespVO.class));
    }

}