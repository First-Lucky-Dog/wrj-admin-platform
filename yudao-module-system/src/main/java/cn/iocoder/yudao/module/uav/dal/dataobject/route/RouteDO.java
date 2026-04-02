package cn.iocoder.yudao.module.uav.dal.dataobject.route;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 航线模板 DO
 *
 * @author 芋道源码
 */
@TableName("uav_route")
@KeySequence("uav_route_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 航线名称
     */
    private String routeName;
    /**
     * 航线类型
     */
    private Integer routeType;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 预计里程
     */
    private BigDecimal estDistanceM;
    /**
     * 预计时长
     */
    private Integer estDurationS;
    /**
     * 备注
     */
    private String remark;


}