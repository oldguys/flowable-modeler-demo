package com.example.oldguy.modules.flow.services.cmd.rollback.impl;

import com.example.oldguy.modules.flow.services.cmd.rollback.AbstractRollbackOperateStrategy;
import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackConstants;
import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackParamsTemplate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.api.history.HistoricTaskInstance;

/**
 * @ClassName: NextDefaultUserTaskRollbackOperateStrategy
 * @Author: huangrenhao
 * @Description: 普通节点 ，兼容嵌入式子流程
 * @CreateTime： 2020/3/25 0025 下午 4:00
 * @Version：
 **/
public class NextDefaultUserTaskRollbackOperateStrategy extends AbstractRollbackOperateStrategy {

    public NextDefaultUserTaskRollbackOperateStrategy(RollbackParamsTemplate paramsTemplate) {
        super(paramsTemplate);
    }

    @Override
    public void createExecution() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        // 获取正在执行 execution
        ExecutionEntity executionEntity = getExecutionEntity();

        ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext).createChildExecution(executionEntity.getParent());
        // 创建新任务
        createExecution(newExecution);
        // 移除历史任务
        removeHisTask(hisTask);
    }

    @Override
    public void setAssignee() {
        // 进行任务执行人配置,之后使用全局监听出发更新
        super.setAssignee();
        String type = RollbackConstants.TASK_TYPE_PREFIX_KEY + paramsTemplate.getHisTask().getProcessInstanceId() + paramsTemplate.getHisTask().getTaskDefinitionKey();
        variables.put(type, NextDefaultUserTaskRollbackOperateStrategy.class.getSimpleName());
    }




}
