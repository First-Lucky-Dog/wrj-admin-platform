package cn.iocoder.yudao.module.uav.controller.admin.mission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 飞行任务新增/修改 Request VO")
@Data
public class MissionSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26257")
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "任务编号不能为空")
    private String missionNo;

    @Schema(description = "设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "26876")
    @NotNull(message = "设备不能为空")
    private Long deviceId;

    @Schema(description = "航线", example = "24311")
    private Long routeId;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务类型不能为空")
    private Integer missionType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "计划开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime startTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime endTime;

    @Schema(description = "实际里程")
    private BigDecimal actualDistanceM;

    @Schema(description = "实际时长")
    private Integer actualDurationS;

    @Schema(description = "失败原因", example = "不对")
    private String failReason;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}