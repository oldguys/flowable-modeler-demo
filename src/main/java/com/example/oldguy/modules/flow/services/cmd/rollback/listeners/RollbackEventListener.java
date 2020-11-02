package com.example.oldguy.modules.flow.services.cmd.rollback.listeners;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackConstants;
import com.example.oldguy.modules.flow.services.cmd.rollback.impl.DefaultTaskNextGatewayRollbackOperateStrategy;
import com.example.oldguy.modules.flow.services.cmd.rollback.impl.NextCallActivityRollbackOperateStrategy;
import com.example.oldguy.modules.flow.services.cmd.rollback.impl.NextDefaultUserTaskRollbackOperateStrategy;
import com.example.oldguy.modules.flow.services.cmd.rollback.impl.NextSubProcessRollbackOperateStrategy;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @ClassName: RollBackListener
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/25 0025 下午 5:45
 * @Version：
 **/
@Component
public class RollbackEventListener implements FlowableEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(RollbackEventListener.class);

    @Override
    public void onEvent(FlowableEvent event) {

        if (FlowableEngineEventType.TASK_CREATED.name().equals(event.getType().name())) {
            TaskEntity taskEntity = (TaskEntity) ((FlowableEntityEventImpl) event).getEntity();

            RuntimeService runtimeService = SpringContextUtils.getBean(RuntimeService.class);
            TaskService taskService = SpringContextUtils.getBean(TaskService.class);

            String key = RollbackConstants.ASSIGNEE_PREFIX_KEY + taskEntity.getProcessInstanceId() + taskEntity.getTaskDefinitionKey();
            String type = RollbackConstants.TASK_TYPE_PREFIX_KEY + taskEntity.getProcessInstanceId() + taskEntity.getTaskDefinitionKey();

            Object assigneeValue = runtimeService.getVariable(taskEntity.getExecutionId(), key);
            Object assigneeType = runtimeService.getVariable(taskEntity.getExecutionId(), type);
            if (assigneeValue != null && assigneeType != null) {
                LOGGER.info("回滚任务处理");
                if (
                        NextDefaultUserTaskRollbackOperateStrategy.class.getSimpleName().equals(assigneeType)
                                || NextSubProcessRollbackOperateStrategy.class.getSimpleName().equals(assigneeType)
                                || NextCallActivityRollbackOperateStrategy.class.getSimpleName().equals(assigneeType)
                                || DefaultTaskNextGatewayRollbackOperateStrategy.class.getSimpleName().equals(assigneeType)
                ) {
                    LOGGER.info("设置普通任务执行人");
                    taskService.setAssignee(taskEntity.getId(), (String) assigneeValue);
                }
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
