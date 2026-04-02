package cn.iocoder.yudao.module.uav.controller.admin.routepoint.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 航线点位分页 Request VO")
@Data
public class RoutePointPageReqVO extends PageParam {

    @Schema(description = "航线", example = "19678")
    private Long routeId;

    @Schema(description = "点位序号")
    private Integer seqNo;

    @Schema(description = "动作类型", example = "1")
    private String actionType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}