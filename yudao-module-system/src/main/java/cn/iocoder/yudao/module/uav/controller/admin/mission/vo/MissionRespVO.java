package cn.iocoder.yudao.module.uav.controller.admin.mission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 飞行任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MissionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26257")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("任务编号")
    private String missionNo;

    @Schema(description = "设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "26876")
    @ExcelProperty("设备")
    private Long deviceId;

    @Schema(description = "航线", example = "24311")
    @ExcelProperty("航线")
    private Long routeId;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("任务类型")
    private Integer missionType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "实际开始时间")
    @ExcelProperty("实际开始时间")
    private LocalDateTime startTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    private LocalDateTime endTime;

    @Schema(description = "实际里程")
    @ExcelProperty("实际里程")
    private BigDecimal actualDistanceM;

    @Schema(description = "实际时长")
    @ExcelProperty("实际时长")
    private Integer actualDurationS;

    @Schema(description = "失败原因", example = "不对")
    @ExcelProperty("失败原因")
    private String failReason;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}