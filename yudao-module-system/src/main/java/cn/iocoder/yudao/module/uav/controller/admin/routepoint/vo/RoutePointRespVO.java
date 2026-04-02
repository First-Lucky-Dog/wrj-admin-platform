package cn.iocoder.yudao.module.uav.controller.admin.routepoint.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 航线点位 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RoutePointRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30282")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "航线", requiredMode = Schema.RequiredMode.REQUIRED, example = "19678")
    @ExcelProperty("航线")
    private Long routeId;

    @Schema(description = "点位序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("点位序号")
    private Integer seqNo;

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

    @Schema(description = "动作类型", example = "1")
    @ExcelProperty(value = "动作类型", converter = DictConvert.class)
    @DictFormat("action_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String actionType;

    @Schema(description = "动作参数")
    @ExcelProperty("动作参数")
    private String actionParam;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}