package cn.iocoder.yudao.module.uav.controller.admin.commandlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 控制指令日志新增/修改 Request VO")
@Data
public class CommandLogSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "28312")
    private Long id;

    @Schema(description = "设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "20528")
    @NotNull(message = "设备不能为空")
    private Long deviceId;

    @Schema(description = "任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "4084")
    @NotNull(message = "任务不能为空")
    private Long missionId;

    @Schema(description = "指令类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "指令类型不能为空")
    private String commandType;

    @Schema(description = "指令参数")
    private String commandPayload;

    @Schema(description = "下发状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "下发状态不能为空")
    private Integer sendStatus;

    @Schema(description = "回执状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "回执状态不能为空")
    private Integer ackStatus;

    @Schema(description = "回执信息")
    private String ackMessage;

    @Schema(description = "操作人ID", example = "9483")
    private Long operatorId;

    @Schema(description = "操作人", example = "李四")
    private String operatorName;

    @Schema(description = "下发时间")
    private LocalDateTime sendTime;

    @Schema(description = "回执时间")
    private LocalDateTime ackTime;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}