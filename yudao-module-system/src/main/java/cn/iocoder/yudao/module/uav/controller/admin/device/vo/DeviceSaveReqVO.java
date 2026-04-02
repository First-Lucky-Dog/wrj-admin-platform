package cn.iocoder.yudao.module.uav.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 无人机设备新增/修改 Request VO")
@Data
public class DeviceSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5055")
    private Long id;

    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备编码不能为空")
    private String deviceCode;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "在线状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "在线状态不能为空")
    private Integer onlineStatus;

    @Schema(description = "飞行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "飞行状态不能为空")
    private Integer flightStatus;

    @Schema(description = "电量")
    private Integer batteryLevel;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "高度")
    private BigDecimal alt;

    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeatTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}