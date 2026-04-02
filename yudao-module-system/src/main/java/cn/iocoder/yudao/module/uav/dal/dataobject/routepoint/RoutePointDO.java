package cn.iocoder.yudao.module.uav.dal.dataobject.routepoint;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 航线点位 DO
 *
 * @author 芋道源码
 */
@TableName("uav_route_point")
@KeySequence("uav_route_point_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePointDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 航线
     */
    private Long routeId;
    /**
     * 点位序号
     */
    private Integer seqNo;
    /**
     * 经度
     */
    private BigDecimal lng;
    /**
     * 纬度
     */
    private BigDecimal lat;
    /**
     * 高度
     */
    private BigDecimal alt;
    /**
     * 速度
     */
    private BigDecimal speedMps;
    /**
     * 动作类型
     *
     * 枚举 {@link TODO action_type 对应的类}
     */
    private String actionType;
    /**
     * 动作参数
     */
    private String actionParam;
    /**
     * 备注
     */
    private String remark;


}