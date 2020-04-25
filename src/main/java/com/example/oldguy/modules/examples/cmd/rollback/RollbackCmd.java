package com.example.oldguy.modules.examples.cmd.rollback;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.examples.exceptions.FlowableRuntimeException;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: RollbackCmd
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/23 0023 下午 3:10
 * @Version：
 **/
public class RollbackCmd implements Command {

    private Logger LOGGER = LoggerFactory.getLogger(RollbackCmd.class);

    /**
     * 任务ID
     */
    private String taskId;

    private String assignee;

    private HistoryService historyService;

    private RuntimeService runtimeService;

    private RollbackStrategyFactory rollbackStrategyFactory;

    /**
     * 会签任务 单个执行人 表达式
     */
    private String assigneeExpr = "assignee";

    /**
     * 会签任务 集合 表达式
     */
    private String assigneeListExpr = "assigneeList";

    public RollbackCmd(String taskId, String assignee) {
        this.taskId = taskId;
        this.assignee = assignee;

        this.historyService = SpringContextUtils.getBean(HistoryService.class);
        this.runtimeService = SpringContextUtils.getBean(RuntimeService.class);
        this.rollbackStrategyFactory = SpringContextUtils.getBean(RollbackStrategyFactory.class);
    }

    @Override
    public Object execute(CommandContext commandContext) {

        HistoricTaskInstance hisTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

        if (null == hisTask.getEndTime()){
            String msg = "任务正在执行,不需要回退";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(hisTask.getProcessInstanceId()).singleResult();
        if (null == pi) {
            String msg = "该流程已经完成，无法进行任务回退。";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

        RollbackOperateStrategy strategy = rollbackStrategyFactory.createStrategy(hisTask);

        // 配置任务执行表达式
        strategy.setAssigneeExpr(assigneeExpr, assigneeListExpr);
        // 处理
        strategy.process(commandContext, assignee);

        // 判断下一节点类型，根据下一节点类型获得任务处理策略

        //

        return null;
    }
}
