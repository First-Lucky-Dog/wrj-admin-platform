package cn.iocoder.yudao.module.uav.controller.admin.trackpoint.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 飞行轨迹点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TrackPointRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "981")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "任务", example = "31233")
    @ExcelProperty("任务")
    private Long missionId;

    @Schema(description = "设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "21802")
    @ExcelProperty("设备")
    private Long deviceId;

    @Schema(description = "轨迹时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("轨迹时间")
    private LocalDateTime trackTime;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("经度")
    private BigDecimal lng;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("纬度")
    private BigDecimal lat;

    @Schema(description = "高度")
    @ExcelProperty("高度")
    private BigDecimal alt;

    @Schema(description = "速度")
    @ExcelProperty("速度")
    private BigDecimal speedMps;

    @Schema(description = "电量")
    @ExcelProperty("电量")
    private Integer batteryLevel;

    @Schema(description = "航向角")
    @ExcelProperty("航向角")
    private BigDecimal heading;

    @Schema(description = "扩展数据")
    @ExcelProperty("扩展数据")
    private String extraJson;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}