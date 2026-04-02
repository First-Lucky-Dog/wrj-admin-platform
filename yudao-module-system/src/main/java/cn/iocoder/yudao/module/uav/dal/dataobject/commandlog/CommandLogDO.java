package cn.iocoder.yudao.module.uav.dal.dataobject.commandlog;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 控制指令日志 DO
 *
 * @author 芋道源码
 */
@TableName("uav_command_log")
@KeySequence("uav_command_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandLogDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 设备
     */
    private Long deviceId;
    /**
     * 任务
     */
    private Long missionId;
    /**
     * 指令类型
     *
     * 枚举 {@link TODO command_type 对应的类}
     */
    private String commandType;
    /**
     * 指令参数
     */
    private String commandPayload;
    /**
     * 下发状态
     */
    private Integer sendStatus;
    /**
     * 回执状态
     */
    private Integer ackStatus;
    /**
     * 回执信息
     */
    private String ackMessage;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 操作人
     */
    private String operatorName;
    /**
     * 下发时间
     */
    private LocalDateTime sendTime;
    /**
     * 回执时间
     */
    private LocalDateTime ackTime;
    /**
     * 备注
     */
    private String remark;


}