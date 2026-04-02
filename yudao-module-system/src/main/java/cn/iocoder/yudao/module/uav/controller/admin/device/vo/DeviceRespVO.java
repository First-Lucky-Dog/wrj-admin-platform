package cn.iocoder.yudao.module.uav.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 无人机设备 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DeviceRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5055")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("设备编码")
    private String deviceCode;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("设备名称")
    private String deviceName;

    @Schema(description = "型号")
    @ExcelProperty("型号")
    private String model;

    @Schema(description = "在线状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("在线状态")
    private Integer onlineStatus;

    @Schema(description = "飞行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("飞行状态")
    private Integer flightStatus;

    @Schema(description = "电量")
    @ExcelProperty("电量")
    private Integer batteryLevel;

    @Schema(description = "经度")
    @ExcelProperty("经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    @ExcelProperty("纬度")
    private BigDecimal lat;

    @Schema(description = "高度")
    @ExcelProperty("高度")
    private BigDecimal alt;

    @Schema(description = "最后心跳时间")
    @ExcelProperty("最后心跳时间")
    private LocalDateTime lastHeartbeatTime;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}