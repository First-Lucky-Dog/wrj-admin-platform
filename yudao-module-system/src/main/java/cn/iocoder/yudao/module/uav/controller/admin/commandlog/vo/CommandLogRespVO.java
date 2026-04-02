package cn.iocoder.yudao.module.uav.controller.admin.commandlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 控制指令日志 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CommandLogRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "28312")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "20528")
    @ExcelProperty("设备")
    private Long deviceId;

    @Schema(description = "任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "4084")
    @ExcelProperty("任务")
    private Long missionId;

    @Schema(description = "指令类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "指令类型", converter = DictConvert.class)
    @DictFormat("command_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String commandType;

    @Schema(description = "指令参数")
    @ExcelProperty("指令参数")
    private String commandPayload;

    @Schema(description = "下发状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("下发状态")
    private Integer sendStatus;

    @Schema(description = "回执状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("回执状态")
    private Integer ackStatus;

    @Schema(description = "回执信息")
    @ExcelProperty("回执信息")
    private String ackMessage;

    @Schema(description = "操作人ID", example = "9483")
    @ExcelProperty("操作人ID")
    private Long operatorId;

    @Schema(description = "操作人", example = "李四")
    @ExcelProperty("操作人")
    private String operatorName;

    @Schema(description = "下发时间")
    @ExcelProperty("下发时间")
    private LocalDateTime sendTime;

    @Schema(description = "回执时间")
    @ExcelProperty("回执时间")
    private LocalDateTime ackTime;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}