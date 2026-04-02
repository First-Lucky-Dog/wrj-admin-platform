package cn.iocoder.yudao.module.uav.controller.admin.commandlog.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 控制指令日志分页 Request VO")
@Data
public class CommandLogPageReqVO extends PageParam {

    @Schema(description = "设备", example = "20528")
    private Long deviceId;

    @Schema(description = "任务", example = "4084")
    private Long missionId;

    @Schema(description = "指令类型", example = "2")
    private String commandType;

    @Schema(description = "下发状态", example = "2")
    private Integer sendStatus;

    @Schema(description = "回执状态", example = "2")
    private Integer ackStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}