-- MySQL 8.x
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS uav_command_log;
DROP TABLE IF EXISTS uav_track_point;
DROP TABLE IF EXISTS uav_mission;
DROP TABLE IF EXISTS uav_route_point;
DROP TABLE IF EXISTS uav_route;
DROP TABLE IF EXISTS uav_device;

-- 1) 无人机设备表
CREATE TABLE uav_device (
                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                            device_code VARCHAR(64) NOT NULL COMMENT '设备编码',
                            device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
                            model VARCHAR(64) DEFAULT NULL COMMENT '型号',
                            online_status TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态:0离线 1在线',
                            flight_status TINYINT NOT NULL DEFAULT 0 COMMENT '飞行状态:0待机 1飞行中 2返航 3故障',
                            battery_level TINYINT DEFAULT NULL COMMENT '电量(0-100)',
                            lng DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
                            lat DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
                            alt DECIMAL(8,2) DEFAULT NULL COMMENT '高度(m)',
                            last_heartbeat_time DATETIME DEFAULT NULL COMMENT '最后心跳时间',
                            remark VARCHAR(500) DEFAULT NULL COMMENT '备注',

                            creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
                            update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                            PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='无人机设备表';

-- 2) 航线模板表
CREATE TABLE uav_route (
                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                           route_name VARCHAR(100) NOT NULL COMMENT '航线名称',
                           route_type TINYINT NOT NULL DEFAULT 1 COMMENT '航线类型:1巡检 2运输 3其他',
                           status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0草稿 1启用 2停用',
                           est_distance_m DECIMAL(12,2) DEFAULT 0 COMMENT '预计里程(米)',
                           est_duration_s INT DEFAULT 0 COMMENT '预计时长(秒)',
                           remark VARCHAR(500) DEFAULT NULL COMMENT '备注',

                           creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                           PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='航线模板表';

-- 3) 航线点位表
CREATE TABLE uav_route_point (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 route_id BIGINT NOT NULL COMMENT '航线ID',
                                 seq_no INT NOT NULL COMMENT '点位序号',
                                 lng DECIMAL(10,7) NOT NULL COMMENT '经度',
                                 lat DECIMAL(10,7) NOT NULL COMMENT '纬度',
                                 alt DECIMAL(8,2) DEFAULT 0 COMMENT '高度(m)',
                                 speed_mps DECIMAL(6,2) DEFAULT 0 COMMENT '速度(m/s)',
                                 action_type VARCHAR(50) DEFAULT NULL COMMENT '动作类型(拍照/悬停等)',
                                 action_param VARCHAR(500) DEFAULT NULL COMMENT '动作参数',
                                 remark VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                 creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                                 PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='航线点位表';

-- 4) 飞行任务表
CREATE TABLE uav_mission (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                             mission_no VARCHAR(64) NOT NULL COMMENT '任务编号',
                             device_id BIGINT NOT NULL COMMENT '设备ID',
                             route_id BIGINT DEFAULT NULL COMMENT '航线ID',
                             mission_type TINYINT NOT NULL DEFAULT 1 COMMENT '任务类型:1按模板 2临时规划',
                             status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待执行 1执行中 2已完成 3失败 4取消',
                             plan_start_time DATETIME DEFAULT NULL COMMENT '计划开始时间',
                             start_time DATETIME DEFAULT NULL COMMENT '实际开始时间',
                             end_time DATETIME DEFAULT NULL COMMENT '实际结束时间',
                             actual_distance_m DECIMAL(12,2) DEFAULT 0 COMMENT '实际里程(米)',
                             actual_duration_s INT DEFAULT 0 COMMENT '实际时长(秒)',
                             fail_reason VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
                             remark VARCHAR(500) DEFAULT NULL COMMENT '备注',

                             creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
                             update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                             PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞行任务表';

-- 5) 飞行轨迹点表（保存近 30 天）
CREATE TABLE uav_track_point (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 mission_id BIGINT DEFAULT NULL COMMENT '任务ID',
                                 device_id BIGINT NOT NULL COMMENT '设备ID',
                                 track_time DATETIME(3) NOT NULL COMMENT '轨迹时间',
                                 lng DECIMAL(10,7) NOT NULL COMMENT '经度',
                                 lat DECIMAL(10,7) NOT NULL COMMENT '纬度',
                                 alt DECIMAL(8,2) DEFAULT 0 COMMENT '高度(m)',
                                 speed_mps DECIMAL(6,2) DEFAULT 0 COMMENT '速度(m/s)',
                                 battery_level TINYINT DEFAULT NULL COMMENT '电量(0-100)',
                                 heading DECIMAL(6,2) DEFAULT NULL COMMENT '航向角',
                                 extra_json JSON DEFAULT NULL COMMENT '扩展数据',

                                 creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                                 PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞行轨迹点表';

-- 6) 控制指令日志表
CREATE TABLE uav_command_log (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 device_id BIGINT NOT NULL COMMENT '设备ID',
                                 mission_id BIGINT DEFAULT NULL COMMENT '任务ID',
                                 command_type VARCHAR(50) NOT NULL COMMENT '指令类型(起飞/返航/悬停/继续等)',
                                 command_payload VARCHAR(1000) DEFAULT NULL COMMENT '指令参数',
                                 send_status TINYINT NOT NULL DEFAULT 0 COMMENT '下发状态:0待下发 1成功 2失败',
                                 ack_status TINYINT NOT NULL DEFAULT 0 COMMENT '回执状态:0无回执 1成功 2失败',
                                 ack_message VARCHAR(500) DEFAULT NULL COMMENT '回执信息',
                                 operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
                                 operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人',
                                 send_time DATETIME DEFAULT NULL COMMENT '下发时间',
                                 ack_time DATETIME DEFAULT NULL COMMENT '回执时间',
                                 remark VARCHAR(500) DEFAULT NULL COMMENT '备注',

                                 creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

                                 PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='控制指令日志表';

SET FOREIGN_KEY_CHECKS = 1;
