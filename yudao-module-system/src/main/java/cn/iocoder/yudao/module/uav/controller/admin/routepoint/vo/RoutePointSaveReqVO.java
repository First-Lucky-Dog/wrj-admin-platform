package cn.iocoder.yudao.module.uav.controller.admin.routepoint.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 航线点位新增/修改 Request VO")
@Data
public class RoutePointSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30282")
    private Long id;

    @Schema(description = "航线", requiredMode = Schema.RequiredMode.REQUIRED, example = "19678")
    @NotNull(message = "航线不能为空")
    private Long routeId;

    @Schema(description = "点位序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "点位序号不能为空")
    private Integer seqNo;

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

    @Schema(description = "动作类型", example = "1")
    private String actionType;

    @Schema(description = "动作参数")
    private String actionParam;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}