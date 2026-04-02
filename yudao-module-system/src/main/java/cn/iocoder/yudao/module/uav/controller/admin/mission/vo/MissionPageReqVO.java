package cn.iocoder.yudao.module.uav.controller.admin.mission.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 飞行任务分页 Request VO")
@Data
public class MissionPageReqVO extends PageParam {

    @Schema(description = "任务编号")
    private String missionNo;

    @Schema(description = "设备", example = "26876")
    private Long deviceId;

    @Schema(description = "航线", example = "24311")
    private Long routeId;

    @Schema(description = "任务类型", example = "1")
    private Integer missionType;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}