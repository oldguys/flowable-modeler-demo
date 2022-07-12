> **场景**：进入flowable开发的时候，经常会遇到一个需求，撤回任务（即：如果下一节点任务未完成，可以回退到上一节点），官方提供了一个可以跳转的api，可实际上并不满足需求，因为可能性特别多如图（ 1-1 ）。如场景：会签，并行网关，调用子流程，嵌入子流程...等情况的时候，会出现跳转失败的问题。本章主要对跳转进行分析解决常见的跳转。
>
> 此处基于 策略模式 和 模板方法模式 对功能进行设计。所以基于5种常见场景，分为6章对于原理进行分析：
> 
> 
> **环境**：
>   springboot：2.2.0.RELEASE
>   flowable：6.4.2
>
> git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)
>
> ###### flowable提供跳转 api
>~~~
>        runtimeService.createChangeActivityStateBuilder()
>               .processInstanceId(hisTask.getProcessInstanceId())
>                .moveActivityIdTo(currentTask.getTaskDefinitionKey(), "e-1")
>                .changeState();
>~~~
>
> ![回退可能性 1-1 ](https://upload-images.jianshu.io/upload_images/14387783-31562218827a0b26.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
>
> 所有章节：
> 1. [flowable 上一节点任务撤回-（1）普通节点撤回](https://www.jianshu.com/p/ee42924ed029)
> 2. [flowable 上一节点任务撤回-（2）会签任务（已完成）](https://www.jianshu.com/p/93fc02bb31d7)
> 2. [flowable 上一节点任务撤回-（3）会签任务（正在执行中）](https://www.jianshu.com/p/6daf767b1084)
> 2. [flowable 上一节点任务撤回-（4）多重网关](https://www.jianshu.com/p/9801acf01ceb)
> 2. [flowable 上一节点任务撤回-（5）嵌入式子流程](https://www.jianshu.com/p/9e7e49c2a8aa)
> 2. [flowable 上一节点任务撤回-（6）调用式子流程](https://www.jianshu.com/p/cf6d10a350cd)


 ##### 前置知识：
**1.** runtime 阶段 常用实体之间的关系：如图（1-2）
 ![实体间关系 1-2 ](https://upload-images.jianshu.io/upload_images/14387783-742b7e522ab55710.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> PS: 其中 Execution 之间的关系是 父子节点 1：n 关系。ProcessInstanceId = 顶级 Execution id
> 
>1. 由上图可以发现，runtime的核心 实际上 是 execution 来的。
>2. 开启流程实例时，流程实例 = Execution 的顶级节点。此时顶级的 Execution会创建子 execution，通过 execution 构建 task 。
>3. ACT_RU_VARIABLE 依赖于 execution, 通常，都会在 ACT_RU_VARIABLE  拿到 Task的 相关构建变量，如 ：设置的表达式变量，或者是会签时候 循环判断变量等
> 



**2.** 由 taskService.complete() 源码可以看到 ，flowable所有的操作都实现于 org.flowable.common.engine.impl.interceptor.Command ，可以通过：
~~~
   @Autowired
   private ManagementService managementService;

   managementService.executeCommand(new RollbackCmd(taskId, assignee));
~~~
进行操作。
3.任务创建可以使用：CommandContextUtil.getAgenda(commandContext).planContinueProcessInCompensation(newExecution);
进行创建。这种方式创建任务是线程安全的。

---
>  ##### 实际操作：
> 由图1可以看出流程跳转之前可能性是非常复杂的，如以下场景：
>1. 简单场景: 2个节点之间进行回退，关系简单，共用同个execution
>2. 会签：会签由 一个 总 execution 和 N个子 execution 组合。
>3. 并行网关汇总：基于并行网关进行流程跳转的时候，也会出现上述的那种情况，总 execution 和 N个子 execution 组合。通过则进入下一节点。
>4. 嵌入子流程：嵌入试子流程节点存在于 SubProcess 节点下的 流程节点信息，需要特殊处理
>5. 调用子流程：调用子流程会新建一个与当前流程弱关联的流程，关联仅仅存在于 execution之间的 superExecution 值
> 6. 复合请求：将上面情况进行随机组成，又变成新的情况 。
>
> 由以上描述就可以看出，情况特别复杂，并且相互交错，可实际操作却只是模拟exection的流转。所以为了方便维护和扩展。可以基于 策略模式，对回退功能进行设计。（如图 1-3）

![任务撤回 1-3](https://upload-images.jianshu.io/upload_images/14387783-cf3fcdb9fbe226ea.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> **接口类描述：**
>  RollbackCmd: 执行回退策略 Command实现类
>  RollbackStrategyFactory： 回退策略工厂
>  RollbackOperateStrategy：回退策略接口
>  RollbackParamsTemplate：回退策略通用构造入参
>  RollbackEventListener：回退监听，配合进行回退任务执行人设置
>

RollbackCmd 调用 RollbackStrategyFactory 根据流程图进行分析，构建出 特定场景的 RollbackOperateStrategy，然后进行 RollbackOperateStrategy.process() 操作。完成任务回退


#### RollbackCmd : 执行回退策略 Command实现类
~~~
package com.example.oldguy.modules.app.plugins.rollback;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.app.exceptions.FlowableRuntimeException;
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
 * @Author: ren
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

~~~

#### DefaultRollbackStrategyFactoryBean： 回退策略工厂

~~~
package com.example.oldguy.modules.app.plugins.rollback;

import com.example.oldguy.modules.app.plugins.rollback.impl.*;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @ClassName: DefaultRollbackStrategyFactoryBean
 * @Author: ren
 * @Description:
 * @CreateTime： 2020/3/23 0023 下午 3:35
 * @Version：
 **/
@Component
public class DefaultRollbackStrategyFactoryBean implements RollbackStrategyFactory {

    private Logger LOGGER = LoggerFactory.getLogger(DefaultRollbackStrategyFactoryBean.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;


    @Override
    public RollbackOperateStrategy createStrategy(HistoricTaskInstance hisTask) {

        BpmnModel bpmnModel = repositoryService.getBpmnModel(hisTask.getProcessDefinitionId());

        RollbackParamsTemplate template = new RollbackParamsTemplate();

        template.setHisTask(hisTask);
        // 获取当前任务的节点信息
        getThisUserTask(template, bpmnModel, hisTask);
        // 获取下一节点信息
        getNextElementInfo(template, bpmnModel);
        // 创建策略
        RollbackOperateStrategy strategy = createStrategyInstance(template);

        return strategy;
    }

    /**
     * @param template
     * @return
     */
    @Override
    public boolean currentMultiInstanceTaskUnfinished(RollbackParamsTemplate template) {

        if (template.getCurrentTaskElement().getLoopCharacteristics() == null) {
            LOGGER.info("当前任务节点不是会签节点");
            return false;
        }

        long count = taskService.createTaskQuery()
                .processInstanceId(template.getHisTask().getProcessInstanceId())
                .taskDefinitionKey(template.getHisTask().getTaskDefinitionKey())
                .count();
        if (count > 0) {
            LOGGER.info("具有未完成当前节点任务");
            return true;
        }

        return false;
    }

    /**
     * 生成策略
     *
     * @param template
     * @return
     */
    private RollbackOperateStrategy createStrategyInstance(RollbackParamsTemplate template) {

        // 处理正在执行会签节点
        if (currentMultiInstanceTaskUnfinished(template)) {
            LOGGER.info("-回退 正在执行会签 策略");
            return new ActiveMultiInstanceTaskRollbackOperateStrategy(template);
        }

        // 默认节点处理策略
        if (template.getCurrentTaskElement().getLoopCharacteristics() == null
                && template.getGatewayMap().isEmpty()
                && !template.getNextUserTaskList().isEmpty()) {
            LOGGER.info("-回退 普通任务 策略");
            return new NextDefaultUserTaskRollbackOperateStrategy(template);
        }

        // 下一节点 嵌入式子流程
        if (template.getCurrentTaskElement().getLoopCharacteristics() == null
                && template.getGatewayMap().isEmpty()
                && !template.getSubProcessMap().isEmpty()) {
            LOGGER.info("-回退 嵌入式子流程 策略");
            return new NextSubProcessRollbackOperateStrategy(template);
        }

        // 下一节点 调用式子流程
        if (template.getCurrentTaskElement().getLoopCharacteristics() == null
                && template.getGatewayMap().isEmpty()
                && !template.getCallActivityMap().isEmpty()) {
            LOGGER.info("-回退 调用式子流程 策略");
            return new NextCallActivityRollbackOperateStrategy(template);
        }

        // 下一节点 网关,多级网关
        if (template.getCurrentTaskElement().getLoopCharacteristics() == null
                && !template.getGatewayMap().isEmpty()) {
            LOGGER.info("-回退 网关, 多级网关 策略");
            return new DefaultTaskNextGatewayRollbackOperateStrategy(template);
        }

        // 会签已完成
        if (template.getCurrentTaskElement().getLoopCharacteristics() != null) {

            if ( template.getGatewayMap().isEmpty()
                    && !template.getNextUserTaskList().isEmpty()) {
                LOGGER.info("-回退 已完成会签,下一节点普通任务 策略");
                return new CompletedMultiInstanceTaskAndNextDefaultTaskRollbackOperateStrategy(template);
            }



            return null;
        }


        return null;
    }


    /**
     * 获取下一节点任务
     *
     * @param template
     * @param bpmnModel
     */
    private void getNextElementInfo(RollbackParamsTemplate template, BpmnModel bpmnModel) {

        if (null != template.getCurrentSubProcess()) {
            LOGGER.info("当前任务存在于 SubProcess");
            getNextElementInfo(template, template.getCurrentSubProcess(), template.getCurrentTaskElement().getOutgoingFlows());
            return;
        }
        LOGGER.info("当前任务存在于 bpmnModel");
        // 主线流程图
        getNextElementInfo(template, bpmnModel.getMainProcess(), template.getCurrentTaskElement().getOutgoingFlows());
    }

    /**
     * 获取下一节点网关任务
     *
     * @param template
     * @param flowElementsContainer
     * @param outgoingFlows
     */
    private void getNextElementInfo(RollbackParamsTemplate template, FlowElementsContainer flowElementsContainer, List<SequenceFlow> outgoingFlows) {

        for (SequenceFlow flow : outgoingFlows) {

            template.getNextFlowIdList().add(flow.getId());
            template.getOutGoingMap().put(flow.getId(), flow);

            // 下一节点
            FlowElement flowElement = flowElementsContainer.getFlowElement(flow.getTargetRef());
            template.getNextFlowIdList().add(flowElement.getId());

            if (flowElement instanceof UserTask) {
                LOGGER.info("下一节点：UserTask");
                template.getNextUserTaskList().add((UserTask) flowElement);
            } else if (flowElement instanceof Gateway) {
                LOGGER.info("下一节点：Gateway");
                Gateway gateway = ((Gateway) flowElement);
                template.getGatewayMap().put(gateway.getId(), gateway);
                getNextElementInfo(template, flowElementsContainer, gateway.getOutgoingFlows());
            } else if (flowElement instanceof SubProcess) {
                LOGGER.info("下一节点：SubProcess");
                SubProcess subProcess = (SubProcess) flowElement;
                template.getSubProcessMap().put(subProcess.getId(), subProcess);
            } else if (flowElement instanceof CallActivity) {
                LOGGER.info("下一节点：CallActivity");
                CallActivity callActivity = (CallActivity) flowElement;
                template.getCallActivityMap().put(callActivity.getId(), callActivity);
            }
        }
    }

    /**
     * 获取当前任务
     *
     * @param template
     * @param bpmnModel
     * @param hisTask
     */
    private void getThisUserTask(RollbackParamsTemplate template, BpmnModel bpmnModel, HistoricTaskInstance hisTask) {

        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(hisTask.getTaskDefinitionKey());
        if (null != flowElement && flowElement instanceof UserTask) {
            LOGGER.info("获取回退任务节点");
            template.setCurrentTaskElement((UserTask) flowElement);
            return;
        }

        for (FlowElement item : bpmnModel.getMainProcess().getFlowElements()) {
            if (item instanceof SubProcess) {
                flowElement = ((SubProcess) item).getFlowElement(hisTask.getTaskDefinitionKey());
                if (null != flowElement) {
                    LOGGER.info("当前节点存在于嵌入式子流程");
                    template.setCurrentTaskElement((UserTask) flowElement);
                    template.setCurrentSubProcess((SubProcess) item);
                    return;
                }
            }
        }

        LOGGER.error("没有获取回退任务节点");


        // TODO  嵌入子流程 场景
    }


}

~~~

#### RollbackOperateStrategy：回退策略接口

~~~
package com.example.oldguy.modules.app.plugins.rollback;

import org.flowable.common.engine.impl.interceptor.CommandContext;

import java.util.Map;

/**
 * @ClassName: RollbackOperateStrategy
 * @Author: ren
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
    void setAssigneeExpr(String assigneeExpr , String assigneeListExpr);

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


~~~

将流程图解析，构建出参数 RollbackParamsTemplate
#### RollbackParamsTemplate：回退策略通用构造入参

~~~
package com.example.oldguy.modules.app.plugins.rollback;

import lombok.Data;
import org.flowable.bpmn.model.*;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.util.*;

/**
 * @ClassName: RollbackParamsTemplate
 * @Author: ren
 * @Description:
 * @CreateTime： 2020/3/23 0023 下午 3:52
 * @Version：
 **/
@Data
public class RollbackParamsTemplate {

    /**
     *  回滚任务
     */
    private HistoricTaskInstance hisTask;

    /**
     *  当前任务节点
     */
    private UserTask currentTaskElement;

    /**
     *  当前节点到下一任务节点间的连线（不包含当前任务节点）
     */
    private Set<String> nextFlowIdList = new HashSet<>();

    /**
     *  当前任务节点到 下一节点 之间线条
     */
    private Map<String, SequenceFlow> outGoingMap = new HashMap<>();

    /**
     *  下一任务节点 集合
     */
    private List<UserTask> nextUserTaskList = new ArrayList<>();

    /**
     *  到下一任务节点 之间的网关集合
     */
    private Map<String, Gateway> gatewayMap = new HashMap<>();

    /**
     *  下一节点是否为 嵌入式子流程
     */
    private Map<String, SubProcess> subProcessMap = new HashMap<>();

    /**
     *  下一节点是否为 调用子流程
     */
    private Map<String, CallActivity> callActivityMap = new HashMap<>();

    /**
     *  当前 嵌入子流程
     */
    private SubProcess currentSubProcess;
}

~~~

#####  RollbackEventListener：回退监听，配合进行回退任务执行人设置
~~~
package com.example.oldguy.modules.app.plugins.rollback.listeners;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.app.plugins.rollback.RollbackConstants;
import com.example.oldguy.modules.app.plugins.rollback.impl.DefaultTaskNextGatewayRollbackOperateStrategy;
import com.example.oldguy.modules.app.plugins.rollback.impl.NextCallActivityRollbackOperateStrategy;
import com.example.oldguy.modules.app.plugins.rollback.impl.NextDefaultUserTaskRollbackOperateStrategy;
import com.example.oldguy.modules.app.plugins.rollback.impl.NextSubProcessRollbackOperateStrategy;
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
 * @Author: ren
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

~~~

---
场景1：普通节点撤回   **NextDefaultUserTaskRollbackOperateStrategy**

![普通节点撤](https://upload-images.jianshu.io/upload_images/14387783-5f7ab2e711ca7ac1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 从 b-1 回退到 a-1
> 步骤：
> 1.  获取 a-1 完成之后创建的 已完成任务列表，如果具有b-1 已完成任务，则不满足前置条件（下一任务节点未完成），无法回退。如果满足，进入下一步骤
> 2.  构建 a-1 的 Execution ，用于构建 Task 
> 3.  移除 ACT_RU_TASK 和 ACT_HI_TASKINST 移除 b-1 任务 和其他依赖 如：VARIABLE ，IDENTITY
> 4. 移除 a-1 原来的历史任务和处理痕迹
> 5. 使用监听器 替换 任务执行人 （操作原理就是 execution存入标识变量，在使用execution构建任务时候，利用监听器，判断是否存在指定标识，具有则，取出设置变量。）

主要代码段：
> Command 中大部分操作 都基于工具类：org.flowable.engine.impl.util.CommandContextUtil
>

##### 基于模板方法模式，构建编写通用 RollbackOperateStrategy.process() 操作
~~~
    @Override
    public void process(CommandContext commandContext, String assignee, Map<String, Object> variables) {

        this.commandContext = commandContext;
        this.assignee = assignee;
        this.variables = variables;

        log.info("处理 existNextFinishedTask");
        existNextFinishedTask();
        log.info("配置任务执行人 setAssignee");
        setAssignee();
        log.info("处理 createExecution");
        createExecution();
        log.info("处理 deleteRuntimeTasks");
        deleteRuntimeTasks();
        log.info("处理 deleteHisActInstance");
        deleteHisActInstance();
    }
~~~


###### 判断是否存在下一节点任务
~~~
    @Override
    public void existNextFinishedTask() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        List<HistoricTaskInstance> hisTaskList = CommandContextUtil.getHistoricTaskService().findHistoricTaskInstancesByQueryCriteria(
                (HistoricTaskInstanceQueryImpl) new HistoricTaskInstanceQueryImpl()
                        .processInstanceId(hisTask.getProcessInstanceId())
                        .taskCompletedAfter(hisTask.getEndTime())
        );

        if (!hisTaskList.isEmpty()) {
            hisTaskList.forEach(obj -> {
                if (paramsTemplate.getNextFlowIdList().contains(obj.getTaskDefinitionKey())) {
                    String msg = "存在已完成下一节点任务";
                    throw new FlowableRuntimeException(msg);
                }
            });
        }
    }
~~~

###### 获取用于构建 a-1 的相邻 execution

~~~
    protected ExecutionEntity getExecutionEntity() {

        ExecutionEntity executionEntity = CommandContextUtil.getExecutionEntityManager(commandContext)
                .findById(paramsTemplate.getHisTask().getExecutionId());

        if (null == executionEntity) {

            log.info("没找到回退任务的 execution,从同级任务处获取");
            List<ExecutionEntity> executionEntityList = CommandContextUtil
                    .getExecutionEntityManager(commandContext)
                    .findExecutionsByParentExecutionAndActivityIds(paramsTemplate.getHisTask().getProcessInstanceId(), paramsTemplate.getNextFlowIdList());
            if (executionEntityList.isEmpty()) {
                throw new FlowableRuntimeException("没有找到临近节点");
            }
            executionEntity = executionEntityList.get(0);
        }

        return executionEntity;
    }
~~~

###### 构建a-1 
~~~
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

    protected void createExecution(ExecutionEntity newExecution) {
        newExecution.setActive(true);
        // 测试设置变量
        newExecution.setVariablesLocal(variables);
        newExecution.setCurrentFlowElement(paramsTemplate.getCurrentTaskElement());

        // 创建新任务
        log.debug("创建新任务");
        CommandContextUtil.getAgenda(commandContext).planContinueProcessInCompensation(newExecution);
    }
~~~

###### 移除正在执行任务及相关连线
~~~
    @Override
    public void deleteRuntimeTasks() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        List<TaskEntity> taskEntityList = CommandContextUtil.getTaskService(commandContext).findTasksByProcessInstanceId(hisTask.getProcessInstanceId());
        taskEntityList.forEach(obj -> {
            if (paramsTemplate.getNextFlowIdList().contains(obj.getTaskDefinitionKey())){
                log.info("移除正在执行的下一节点任务");
                // 移除任务
                removeRuntimeTaskOperate(obj);
            }
        });


        // 移除历史任务信息
        List<HistoricTaskInstanceEntity> historicTaskInstanceList = CommandContextUtil.getHistoricTaskService(commandContext)
                .findHistoricTasksByProcessInstanceId(hisTask.getProcessInstanceId());
        historicTaskInstanceList.forEach(obj->{
            if (paramsTemplate.getNextFlowIdList().contains(obj.getTaskDefinitionKey())){
                CommandContextUtil.getHistoricTaskService(commandContext).deleteHistoricTask(obj);
            }
        });
    }
~~~

以上 完成对 上一节点任务撤回 架构设计 及 第一种情况 解决方案讲述。
git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)