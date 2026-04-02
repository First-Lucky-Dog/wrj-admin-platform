package cn.iocoder.yudao.module.uav.dal.dataobject.mission;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 飞行任务 DO
 *
 * @author 芋道源码
 */
@TableName("uav_mission")
@KeySequence("uav_mission_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 任务编号
     */
    private String missionNo;
    /**
     * 设备
     */
    private Long deviceId;
    /**
     * 航线
     */
    private Long routeId;
    /**
     * 任务类型
     */
    private Integer missionType;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;
    /**
     * 实际开始时间
     */
    private LocalDateTime startTime;
    /**
     * 实际结束时间
     */
    private LocalDateTime endTime;
    /**
     * 实际里程
     */
    private BigDecimal actualDistanceM;
    /**
     * 实际时长
     */
    private Integer actualDurationS;
    /**
     * 失败原因
     */
    private String failReason;
    /**
     * 备注
     */
    private String remark;


}