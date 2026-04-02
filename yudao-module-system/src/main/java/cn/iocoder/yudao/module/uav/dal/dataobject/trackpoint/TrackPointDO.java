package cn.iocoder.yudao.module.uav.dal.dataobject.trackpoint;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 飞行轨迹点 DO
 *
 * @author 芋道源码
 */
@TableName("uav_track_point")
@KeySequence("uav_track_point_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackPointDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 任务
     */
    private Long missionId;
    /**
     * 设备
     */
    private Long deviceId;
    /**
     * 轨迹时间
     */
    private LocalDateTime trackTime;
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
     * 电量
     */
    private Integer batteryLevel;
    /**
     * 航向角
     */
    private BigDecimal heading;
    /**
     * 扩展数据
     */
    private String extraJson;


}