package com.example.oldguy.modules.flow.services.cmd.rollback;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.exceptions.FlowRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;

/**
 * @ClassName: RollbackCmd
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/23 0023 下午 3:10
 * @Version：
 **/
@Slf4j
public class RollbackCmd implements Command<Object> {

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
            log.error(msg);
            throw new FlowRuntimeException(msg);
        }

        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(hisTask.getProcessInstanceId()).singleResult();
        if (null == pi) {
            String msg = "该流程已经完成，无法进行任务回退。";
            log.error(msg);
            throw new FlowRuntimeException(msg);
        }

        RollbackOperateStrategy strategy = rollbackStrategyFactory.createStrategy(hisTask);

        // 配置任务执行表达式
        strategy.setAssigneeExpr(assigneeExpr, assigneeListExpr);
        // 处理
        strategy.process(commandContext, assignee);

        return null;
    }
}
