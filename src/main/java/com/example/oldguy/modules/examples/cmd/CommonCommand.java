package com.example.oldguy.modules.examples.cmd;

import com.example.oldguy.common.utils.SpringContextUtils;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;

/**
 * @ClassName: CommonCommand
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/12 0012 上午 9:21
 * @Version：
 **/
public abstract class CommonCommand<T>  implements Command<T> {

    protected RuntimeService runtimeService;

    protected TaskService taskService;

    protected RepositoryService repositoryService;

    protected ProcessEngine processEngine;

    public CommonCommand() {

        processEngine = SpringContextUtils.getBean(ProcessEngine.class);
        runtimeService = SpringContextUtils.getBean(RuntimeService.class);
        taskService = SpringContextUtils.getBean(TaskService.class);
        repositoryService = SpringContextUtils.getBean(RepositoryService.class);
    }

//
//    /**
//     * 转换成为会签任务
//     *
//     * @param userTask
//     */
//    protected void trainToMultiInstanceTask(UserTask userTask) {
//
//        // 多任务处理
//        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
//        multiInstanceLoopCharacteristics.setSequential(true);
//        multiInstanceLoopCharacteristics.setInputDataItem(AddTaskConstants.ASSIGNEE_LIST_EXPR);
//        multiInstanceLoopCharacteristics.setElementVariable(AddTaskConstants.ASSIGNEE_FLAG);
//
//        // 会签行为解释器
//        ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
//        UserTaskActivityBehavior userTaskActivityBehavior = processEngineConfiguration.getActivityBehaviorFactory().createUserTaskActivityBehavior(userTask);
//        MultiInstanceActivityBehavior behavior = new SequentialMultiInstanceBehavior(userTask, userTaskActivityBehavior);
//
//        // 设置表达式变量
//        ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
//        behavior.setCollectionExpression(expressionManager.createExpression(AddTaskConstants.ASSIGNEE_LIST_EXPR));
//        behavior.setCollectionElementVariable(AddTaskConstants.ASSIGNEE_FLAG);
//
//        // 注入到任务节点中
//        userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
//        userTask.setBehavior(behavior);
//    }
}
