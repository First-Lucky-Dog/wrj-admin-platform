package cn.iocoder.yudao.module.uav.controller.admin.trackpoint.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 飞行轨迹点新增/修改 Request VO")
@Data
public class TrackPointSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "981")
    private Long id;

    @Schema(description = "任务", example = "31233")
    private Long missionId;

    @Schema(description = "设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "21802")
    @NotNull(message = "设备不能为空")
    private Long deviceId;

    @Schema(description = "轨迹时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "轨迹时间不能为空")
    private LocalDateTime trackTime;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经度不能为空")
    private BigDecimal lng;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "纬度不能为空")
    private BigDecimal lat;

    @Schema(description = "高度")
    private BigDecimal alt;

    @Schema(description = "速度")
    private BigDecimal speedMps;

    @Schema(description = "电量")
    private Integer batteryLevel;

    @Schema(description = "航向角")
    private BigDecimal heading;

    @Schema(description = "扩展数据")
    private String extraJson;

}