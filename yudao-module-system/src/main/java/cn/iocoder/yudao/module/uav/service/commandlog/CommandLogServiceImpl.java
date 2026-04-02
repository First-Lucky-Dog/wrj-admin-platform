package cn.iocoder.yudao.module.uav.service.commandlog;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.uav.controller.admin.commandlog.vo.*;
import cn.iocoder.yudao.module.uav.dal.dataobject.commandlog.CommandLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.uav.dal.mysql.commandlog.CommandLogMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;

/**
 * 控制指令日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CommandLogServiceImpl implements CommandLogService {

    @Resource
    private CommandLogMapper commandLogMapper;

    @Override
    public Long createCommandLog(CommandLogSaveReqVO createReqVO) {
        // 插入
        CommandLogDO commandLog = BeanUtils.toBean(createReqVO, CommandLogDO.class);
        commandLogMapper.insert(commandLog);

        // 返回
        return commandLog.getId();
    }

    @Override
    public void updateCommandLog(CommandLogSaveReqVO updateReqVO) {
        // 校验存在
        validateCommandLogExists(updateReqVO.getId());
        // 更新
        CommandLogDO updateObj = BeanUtils.toBean(updateReqVO, CommandLogDO.class);
        commandLogMapper.updateById(updateObj);
    }

    @Override
    public void deleteCommandLog(Long id) {
        // 校验存在
        validateCommandLogExists(id);
        // 删除
        commandLogMapper.deleteById(id);
    }

    @Override
        public void deleteCommandLogListByIds(List<Long> ids) {
        // 删除
        commandLogMapper.deleteByIds(ids);
        }


    private void validateCommandLogExists(Long id) {
        if (commandLogMapper.selectById(id) == null) {
            throw exception(new ErrorCode(401, "控制指令日志不存在"));
        }
    }

    @Override
    public CommandLogDO getCommandLog(Long id) {
        return commandLogMapper.selectById(id);
    }

    @Override
    public PageResult<CommandLogDO> getCommandLogPage(CommandLogPageReqVO pageReqVO) {
        return commandLogMapper.selectPage(pageReqVO);
    }

}