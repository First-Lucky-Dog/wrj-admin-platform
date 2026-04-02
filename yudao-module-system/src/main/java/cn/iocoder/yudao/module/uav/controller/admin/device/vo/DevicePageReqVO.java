package cn.iocoder.yudao.module.uav.controller.admin.device.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 无人机设备分页 Request VO")
@Data
public class DevicePageReqVO extends PageParam {

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称", example = "赵六")
    private String deviceName;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "在线状态", example = "1")
    private Integer onlineStatus;

    @Schema(description = "飞行状态", example = "1")
    private Integer flightStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}