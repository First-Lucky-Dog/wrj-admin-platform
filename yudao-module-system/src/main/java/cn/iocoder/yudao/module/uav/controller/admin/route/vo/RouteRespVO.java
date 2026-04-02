package cn.iocoder.yudao.module.uav.controller.admin.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 航线模板 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RouteRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21598")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "航线名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("航线名称")
    private String routeName;

    @Schema(description = "航线类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("航线类型")
    private Integer routeType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "预计里程")
    @ExcelProperty("预计里程")
    private BigDecimal estDistanceM;

    @Schema(description = "预计时长")
    @ExcelProperty("预计时长")
    private Integer estDurationS;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}