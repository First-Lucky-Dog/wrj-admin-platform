package cn.iocoder.yudao.module.uav.dal.dataobject.device;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 无人机设备 DO
 *
 * @author 芋道源码
 */
@TableName("uav_device")
@KeySequence("uav_device_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 设备编码
     */
    private String deviceCode;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 型号
     */
    private String model;
    /**
     * 在线状态
     */
    private Integer onlineStatus;
    /**
     * 飞行状态
     */
    private Integer flightStatus;
    /**
     * 电量
     */
    private Integer batteryLevel;
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
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;
    /**
     * 备注
     */
    private String remark;


}