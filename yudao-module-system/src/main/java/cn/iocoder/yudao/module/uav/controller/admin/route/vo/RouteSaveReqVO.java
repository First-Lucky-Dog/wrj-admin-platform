package cn.iocoder.yudao.module.uav.controller.admin.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 航线模板新增/修改 Request VO")
@Data
public class RouteSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21598")
    private Long id;

    @Schema(description = "航线名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "航线名称不能为空")
    private String routeName;

    @Schema(description = "航线类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "航线类型不能为空")
    private Integer routeType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "预计里程")
    private BigDecimal estDistanceM;

    @Schema(description = "预计时长")
    private Integer estDurationS;

    @Schema(description = "备注", example = "随便")
    private String remark;

}