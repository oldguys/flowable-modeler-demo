package com.example.oldguy.modules.flow.services.cmd.rollback;

import org.flowable.common.engine.impl.interceptor.CommandContext;

import java.util.Map;

/**
 * @ClassName: RollbackOperateStrategy
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/23 0023 下午 3:35
 * @Version：
 **/
public interface RollbackOperateStrategy {


    /**
     * 处理
     */
    void process(CommandContext commandContext, String assignee);

    /**
     * 处理
     */
    void process(CommandContext commandContext, String assignee, Map<String, Object> variables);


    /**
     *  配置处理标识
     * @param assigneeExpr
     * @param assigneeListExpr
     */
    void setAssigneeExpr(String assigneeExpr, String assigneeListExpr);

    /**
     *  配置任务处理人
     */
    void setAssignee();

    /**
     * 移除相关关联
     */
    void existNextFinishedTask();

    /**
     * 移除历史痕迹
     */
    void deleteHisActInstance();

    /**
     * 移除正在运行的任务
     */
    void deleteRuntimeTasks();

    /**
     * 创建任务
     */
    void createExecution();

}
