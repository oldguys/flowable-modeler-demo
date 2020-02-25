package com.example.oldguy.modules.examples.cmd;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.examples.constants.AddTaskConstants;
import com.example.oldguy.modules.examples.exceptions.FlowableRuntimeException;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @ClassName: RollbackTaskCommand
 * @Author: huangrenhao
 * @Description: 流程跳转回当前节点
 * @CreateTime： 2020/2/14 0014 下午 2:36
 * @Version：
 **/
public class RollbackTaskCommand implements Command<String> {

    private static Logger LOGGER = LoggerFactory.getLogger(RollbackTaskCommand.class);

    private String taskId;

    private RuntimeService runtimeService;

    private RepositoryService repositoryService;

    private HistoryService historyService;

    private TaskService taskService;

    private String assignee;

    private String currentTaskDefinitionKey;

    private Map<String, Gateway> gatewayMap = new HashMap<>();

    private List<Task> currentDefinitionKeyTaskList;

    private boolean hasNextTask = false;

    private Set<String> subProcessDefinitionKeySet = new HashSet<>();

    private Map<String, SubProcess> subProcessMap = new HashMap<>();

    private String currentSubProcessId;

    private boolean isEndElement = false;

    /**
     * 特殊参数标识
     */

    private String ASSIGNEE_LIST;
    private String ASSIGNEE_LIST_EXPR;
    private String ASSIGNEE;
    private String ASSIGNEE_EXPR;

    public RollbackTaskCommand(String taskId, String assignee) {
        this.taskId = taskId;
        this.assignee = assignee;

        runtimeService = SpringContextUtils.getBean(RuntimeService.class);
        repositoryService = SpringContextUtils.getBean(RepositoryService.class);
        historyService = SpringContextUtils.getBean(HistoryService.class);
        taskService = SpringContextUtils.getBean(TaskService.class);

        /**
         *  初始化表达式
         */
        ASSIGNEE_LIST = AddTaskConstants.ASSIGNEE_LIST;
        ASSIGNEE_LIST_EXPR = AddTaskConstants.ASSIGNEE_LIST_EXPR;
        ASSIGNEE = AddTaskConstants.ASSIGNEE_FLAG;
        ASSIGNEE_EXPR = AddTaskConstants.ASSIGNEE_EXPR;
    }

    public RollbackTaskCommand(String taskId, String assignee, Map<String, String> paramsReflect) {
        this.taskId = taskId;
        this.assignee = assignee;

        runtimeService = SpringContextUtils.getBean(RuntimeService.class);
        repositoryService = SpringContextUtils.getBean(RepositoryService.class);
        historyService = SpringContextUtils.getBean(HistoryService.class);
        taskService = SpringContextUtils.getBean(TaskService.class);

        if (paramsReflect != null) {
            String value = paramsReflect.get(AddTaskConstants.ASSIGNEE_LIST);
            if (!StringUtils.isEmpty(value)) {
                ASSIGNEE_LIST = value;
            }
            value = paramsReflect.get(AddTaskConstants.ASSIGNEE_LIST_EXPR);
            if (!StringUtils.isEmpty(value)) {
                ASSIGNEE_LIST_EXPR = value;
            }
            value = paramsReflect.get(AddTaskConstants.ASSIGNEE_FLAG);
            if (!StringUtils.isEmpty(value)) {
                ASSIGNEE = value;
            }
            value = paramsReflect.get(AddTaskConstants.ASSIGNEE_EXPR);
            if (!StringUtils.isEmpty(value)) {
                ASSIGNEE_EXPR = value;
            }
        }

    }


    @Override
    public String execute(CommandContext commandContext) {

        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

        if (null == task) {
            String msg = "没有找到任务 [ task = " + taskId + " ]";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

        if (null == task.getEndTime()) {
            String msg = "任务 [ task = " + taskId + " ] 正在执行中";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        if (null == pi) {
            String msg = "没有找到正直执行的实例 [ pi = " + task.getProcessInstanceId() + " ]";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

        // 获取已完成的 任务列表
        List<HistoricTaskInstance> hisTaskList = historyService.createHistoricTaskInstanceQuery().processInstanceId(pi.getId())
                .taskCompletedAfter(task.getEndTime()) // 获取比改任务执行时间之后的任务列表
                .finished() // 已完成 任务
                .list();

        // 获取任务节点
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        UserTask hisUserTask = (UserTask) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        if (null == hisUserTask) {
            hisUserTask = getFromSubProcess(bpmnModel, task.getTaskDefinitionKey());
        }

        // 获取下一任务节点标识
        Set<String> nextDefinitionKeySet = new HashSet<>();
        getNestUserTaskDefinitionKeySet(bpmnModel, hisUserTask.getOutgoingFlows(), nextDefinitionKeySet);
        if (!StringUtils.isEmpty(currentSubProcessId)) {
            getNestUserTaskDefinitionKeySet(bpmnModel, subProcessMap.get(currentSubProcessId), hisUserTask.getOutgoingFlows(), nextDefinitionKeySet);
        }

        currentDefinitionKeyTaskList = taskService.createTaskQuery()
                .processInstanceId(task.getProcessInstanceId())
                .taskDefinitionKey(task.getTaskDefinitionKey()).list();

        Execution hisExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        if (hisExecution == null) {
            LOGGER.info("没有找到任务execution,获取流程主线的execution");
            List<Execution> list = runtimeService.createExecutionQuery().processInstanceId(task.getProcessInstanceId()).onlyProcessInstanceExecutions().list();
            hisExecution = list.get(0);
        }
        // 配置变量
        Map<String, Object> variables = new HashMap<>();
        // 会签临时节点
        boolean newExecutionEntity = true;

        if (hisTaskList.isEmpty()) {
            LOGGER.info("不存在下一节点已完成任务,可以执行回退");

            // 会签处理
            if (null != hisUserTask.getLoopCharacteristics()) {

                LOGGER.info("0-会签处理");
                // 处理 会签
                newExecutionEntity = processMultiInstanceTaskRollback(task, hisUserTask, hisExecution, variables, false, nextDefinitionKeySet);

            } else {
                LOGGER.info("0-普通节点处理");
                // 配置任务执行人
                variables.put(ASSIGNEE, assignee);
            }

            // 移除正在执行的任务
            removeProcessingTasks(task.getProcessInstanceId(), nextDefinitionKeySet);
            // 移除当前任务
            CommandContextUtil.getHistoricTaskService().deleteHistoricTask((HistoricTaskInstanceEntity) task);
            // 创建新任务
            if (newExecutionEntity) {
                createNewExecution(hisExecution, hisUserTask, variables);
            }
        } else {

            LOGGER.info("1-存在下一节点已完成任务");
            Set<String> completedTaskDefinitionKeySet = new HashSet<>();
            hisTaskList.forEach(obj -> {
                completedTaskDefinitionKeySet.add(obj.getTaskDefinitionKey());
            });

            // 如果已经存在下一节点执行任务,无法回退
            completedTaskDefinitionKeySet.stream().forEach(obj -> {
                if (nextDefinitionKeySet.contains(obj)) {
                    String msg = "如果已经存在下一节点执行任务,无法回退";
                    LOGGER.error(msg);
                    throw new FlowableRuntimeException(msg);
                }
            });

            // 处理会签任务回退
            if (null != hisUserTask.getLoopCharacteristics()) {

                LOGGER.info("1-会签处理");
                // 处理 会签
                newExecutionEntity = processMultiInstanceTaskRollback(task, hisUserTask, hisExecution, variables, true, nextDefinitionKeySet);

            } else {
                variables.put(ASSIGNEE, assignee);
            }
            // 移除正在执行的任务
            removeProcessingTasks(task.getProcessInstanceId(), nextDefinitionKeySet);
            // 创建新任务
            if (newExecutionEntity) {
                createNewExecution(hisExecution, hisUserTask, variables);
            }
            // 移除当前任务
            CommandContextUtil.getHistoricTaskService().deleteHistoricTask((HistoricTaskInstanceEntity) task);
        }

        // 如果当前 任务未存在并且 具有网关, 移除 并行网关 executionId
        if (currentDefinitionKeyTaskList.isEmpty() && gatewayMap.size() > 0) {
            removeGatewayLink(task, hisUserTask);
        }

        return "回退成功!";
    }

    /**
     * 获取子流程节点
     *
     * @param bpmnModel
     * @param taskDefinitionKey
     * @return
     */
    private UserTask getFromSubProcess(BpmnModel bpmnModel, String taskDefinitionKey) {

        Map<String, UserTask> userTaskMap = new HashMap<>();

        bpmnModel.getMainProcess().getFlowElements().stream().forEach(obj -> {
            if (obj instanceof SubProcess) {

                SubProcess subProcess = (SubProcess) obj;
                FlowElement flowElement = subProcess.getFlowElement(taskDefinitionKey);
                if (null != flowElement) {
                    if (flowElement instanceof UserTask) {
                        userTaskMap.put(obj.getId(), (UserTask) flowElement);
                    }
                }
                subProcessMap.put(obj.getId(), subProcess);
            }
        });

        if (userTaskMap.isEmpty()) {
            String msg = "没有找打指定节点 [ id = " + taskDefinitionKey + " ]  ";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        } else if (userTaskMap.size() > 1) {
            String msg = "超过一个子流程 具有 当前节点 [ id = " + taskDefinitionKey + " ] 无法继续回退";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

        Iterator<String> iterator = userTaskMap.keySet().iterator();
        while (iterator.hasNext()) {
            currentSubProcessId = iterator.next();
        }



        return userTaskMap.get(currentSubProcessId);
    }

    /**
     * 处理会签
     *
     * @param task
     * @param hisUserTask
     * @param hisExecution
     * @param variables
     * @param hasOtherCompleteTask
     */
    private boolean processMultiInstanceTaskRollback(HistoricTaskInstance task, UserTask hisUserTask, Execution hisExecution, Map<String, Object> variables, boolean hasOtherCompleteTask, Set<String> nextDefinitionKeySet) {

        int nrOfActiveInstances;
        int nrOfCompletedInstances;
        int loopCounter;

        if (StringUtils.isEmpty(hisExecution.getParentId())) {
            // 会签任务已经完成
            LOGGER.info("会签任务已经完成");

            List<String> assignees = new ArrayList<>();
            //  测试 executionId
            Object object = runtimeService.getVariable(task.getProcessInstanceId(), AddTaskConstants.HIS_FLAG + ASSIGNEE_LIST);
//            Object object = runtimeService.getVariable(task.getExecutionId(), AddTaskConstants.HIS_FLAG);
            if (object == null) {
                assignees = getMultiInstanceTaskParams(hisExecution);
                //  测试 executionId
                runtimeService.setVariable(task.getProcessInstanceId(), AddTaskConstants.HIS_FLAG + ASSIGNEE_LIST, assignees);
//                runtimeService.setVariable(task.getExecutionId(), AddTaskConstants.HIS_FLAG, assignees);
            } else if (object instanceof List) {
                assignees = (List<String>) object;
            }

            // 获取当然任务执行位置
            int index = assignees.indexOf(assignee);
            if (index == -1) {
                String msg = "未找到当前任务执行人";
                LOGGER.error(msg);
                throw new FlowableRuntimeException(msg);
            }

            // 没有重新回退的任务,直接新建任务链
            if (currentDefinitionKeyTaskList.isEmpty()) {

                if (hisUserTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    // 获取当前 执行人位置
                    if (index != assignees.size() - 1) {
                        String msg = "之前任务已经审批,无法进行回退";
                        LOGGER.error(msg);
                        throw new FlowableRuntimeException(msg);
                    }
                }

                List<String> newAssignees = new ArrayList<>();
                newAssignees.add(assignee);
                variables.put(ASSIGNEE_LIST, newAssignees);
                // 测试 executionId
                runtimeService.setVariableLocal(task.getProcessInstanceId(), ASSIGNEE_LIST, newAssignees);
//                runtimeService.setVariableLocal(task.getExecutionId(), AddTaskConstants.ASSIGNEE_LIST, newAssignees);

            } else {
                // 具有新回退的任务,进行加签

                if (hisUserTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    // 获取当前 执行人位置

                    Task currentTask = currentDefinitionKeyTaskList.get(0);
                    ExecutionEntity executionEntity = CommandContextUtil.getExecutionEntityManager().findById(currentTask.getExecutionId());

                    int assigneeIndex = assignees.indexOf(assignee);

                    nrOfActiveInstances = (int) runtimeService.getVariable(executionEntity.getParent().getId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES);
                    int numberOfInstances = (int) runtimeService.getVariable(executionEntity.getParent().getId(), AddTaskConstants.NUMBER_OF_INSTANCES);


                    if (assigneeIndex > -1 && (assigneeIndex == assignees.size() - numberOfInstances - 1)) {
                        LOGGER.info("执行-串行-加签");

                        runtimeService.setVariableLocal(executionEntity.getParent().getId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances + 1);
                        runtimeService.setVariableLocal(executionEntity.getParent().getId(), AddTaskConstants.NUMBER_OF_INSTANCES, numberOfInstances + 1);
                        runtimeService.setVariableLocal(executionEntity.getId(), ASSIGNEE, assignee);
                        taskService.setAssignee(currentTask.getId(), assignee);

                        // 测试 executionId
                        List<String> currentAssignees = (List<String>) runtimeService.getVariable(task.getProcessInstanceId(), ASSIGNEE_LIST);
//                        List<String> currentAssignees = (List<String>) runtimeService.getVariable(executionEntity.getParent().getId(), AddTaskConstants.ASSIGNEE_LIST);

                        List<String> newAssignees = new ArrayList<>();
                        newAssignees.add(assignee);
                        newAssignees.addAll(currentAssignees);

//                        runtimeService.setVariableLocal(executionEntity.getParent().getId(), ASSIGNEE_LIST, newAssignees);
                        runtimeService.setVariableLocal(task.getProcessInstanceId(), ASSIGNEE_LIST, newAssignees);

                    } else {

                        String msg = "之前任务已经审批,无法进行回退";
                        LOGGER.error(msg);
                        throw new FlowableRuntimeException(msg);

                    }

                    return false;
                } else {
                    LOGGER.info("执行-并行-加签");
                    Task lastTask = currentDefinitionKeyTaskList.get(currentDefinitionKeyTaskList.size() - 1);

                    Execution execution = runtimeService.createExecutionQuery().executionId(lastTask.getExecutionId()).singleResult();

                    Map<String, Object> params = new HashMap<>();
                    params.put(ASSIGNEE, assignee);

                    // 会签 - 加签
                    runtimeService.addMultiInstanceExecution(lastTask.getTaskDefinitionKey(), execution.getProcessInstanceId(), params);

                }

                return false;
            }


        } else {
            // 会签任务正在执行中
            LOGGER.info("会签任务正在执行中");
            nrOfActiveInstances = (int) runtimeService.getVariable(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES);
            nrOfCompletedInstances = (int) runtimeService.getVariable(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_COMPLETED_INSTANCES);
            loopCounter = (int) runtimeService.getVariable(hisExecution.getId(), AddTaskConstants.LOOP_COUNTER);


            if (hisUserTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                LOGGER.info("回退-并行会签任务");

                runtimeService.setVariableLocal(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances + 1);
                runtimeService.setVariableLocal(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances - 1);

                variables.put(ASSIGNEE, assignee);
                variables.put(AddTaskConstants.LOOP_COUNTER, loopCounter);
            } else if (hisUserTask.getBehavior() instanceof SequentialMultiInstanceBehavior) {

                // 串行会签需要移除当前任务
                currentTaskDefinitionKey = hisUserTask.getId();

                if (!hasOtherCompleteTask) {
                    LOGGER.info("0-回退-串行会签任务");

                    runtimeService.setVariableLocal(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances - 1);
                    runtimeService.setVariableLocal(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances - 1);

                    variables.put(ASSIGNEE, assignee);
                    variables.put(AddTaskConstants.LOOP_COUNTER, loopCounter - 1);
                } else {
                    LOGGER.info("1-回退-串行会签任务");
                    List<String> assignees = getMultiInstanceTaskParams(hisExecution);

                    // 获取当前 执行人位置
                    int currentIndex = sequentialTaskAvailable(task, loopCounter, assignees);

                    runtimeService.setVariableLocal(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances - 1);
                    runtimeService.setVariableLocal(hisExecution.getParentId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES, nrOfActiveInstances - 1);
                    variables.put(ASSIGNEE, assignee);
                    variables.put(AddTaskConstants.LOOP_COUNTER, currentIndex);
                }
            }
        }
        return true;
    }

    /**
     * 如果下一节点是网关，则需要移除初次回退创建多余的连线 executionId
     *
     * @param task
     * @param hisUserTask
     */
    private void removeGatewayLink(HistoricTaskInstance task, UserTask hisUserTask) {

        for (SequenceFlow flow : hisUserTask.getOutgoingFlows()) {

            Gateway gateway = gatewayMap.get(flow.getTargetFlowElement().getId());

            if (null != gateway && gateway instanceof ParallelGateway) {

                List<Execution> executions = runtimeService.createExecutionQuery()
                        .processInstanceId(task.getProcessInstanceId())
//                        .activityId(gateway.getId()) // 无效
                        .list();

                if (!executions.isEmpty()) {

                    for (Execution execution : executions) {

                        if (gateway.getId().equals(execution.getActivityId())) {
                            CommandContextUtil.getExecutionEntityManager().delete(execution.getId());
                            break;
                        }
                    }
                }
            }

        }

    }

    private int sequentialTaskAvailable(HistoricTaskInstance task, int loopCounter, List<String> assignees) {
        int currentIndex = assignees.indexOf(task.getAssignee());
        if (currentIndex == -1) {
            String msg = "在该会签任务中没有找到执行人 ：" + task.getAssignee();
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }
        if (loopCounter - currentIndex > 1) {
            String msg = "该任务下一审批人已经审批,无法回退";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

        return currentIndex;
    }

    private List<String> getMultiInstanceTaskParams(Execution hisExecution) {
        Object values = runtimeService.getVariable(hisExecution.getProcessInstanceId(), ASSIGNEE_LIST);
        if (null == values || !(values instanceof List)) {
            String msg = "只有找到会签执行人列表 数据";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }
        return (List<String>) values;
    }


    /**
     * 移除正在执行中任务和历史任务
     *
     * @param processInstanceId
     * @param nextDefinitionKeySet
     */
    private void removeProcessingTasks(String processInstanceId, Set<String> nextDefinitionKeySet) {
        // 获取正在执行任务列表
        List<TaskEntity> taskEntityList = CommandContextUtil.getTaskService().findTasksByProcessInstanceId(processInstanceId);
        Map<String, HistoricTaskInstanceEntity> deletedHisTaskMap = new HashMap<>();

        // 待移除的 父级 executions
        List<ExecutionEntity> parentExecutions = new ArrayList<>();

        taskEntityList.forEach(obj -> {

            boolean flag = nextDefinitionKeySet.contains(obj.getTaskDefinitionKey());

            // 已经生成了下一节点任务
            if (flag) {
                hasNextTask = true;
            }

            // 子流程需要移除 子流程 父级 execution
            if (subProcessDefinitionKeySet.contains(obj.getTaskDefinitionKey())) {
                ExecutionEntity executionEntity = CommandContextUtil.getExecutionEntityManager().findById(obj.getExecutionId());
                if (executionEntity.getParent() != null) {
                    parentExecutions.add(executionEntity.getParent());
                }
                flag = true;
            }

            // 串行会签需要移除当前 任务
            if (flag || obj.getTaskDefinitionKey().equals(currentTaskDefinitionKey)) {
                deletedHisTaskMap.put(obj.getId(), null);

                // 一般情况下 不可能 出现 没有 executionId 的情况
                if (!StringUtils.isEmpty(obj.getExecutionId())) {

                    // 移除任务相关信息
                    CommandContextUtil.getIdentityLinkService().deleteIdentityLinksByTaskId(obj.getId());
                    // 移除正在执行任务列表
                    CommandContextUtil.getTaskService().deleteTasksByExecutionId(obj.getExecutionId());
                    // 删除相关变量
                    CommandContextUtil.getVariableService().deleteVariablesByExecutionId(obj.getExecutionId());
                    // 删除 execution
                    CommandContextUtil.getExecutionEntityManager().delete(obj.getExecutionId());
                } else {
                    // 创建子任务 没有 executionId 需要特殊移除
                    LOGGER.info("删除子任务。。");
                    taskService.deleteTask(obj.getId());
                }
            }
        });

        parentExecutions.forEach(obj -> {
            // 删除相关变量
            CommandContextUtil.getVariableService().deleteVariablesByExecutionId(obj.getId());
            // 删除父级 execution
            CommandContextUtil.getExecutionEntityManager().delete(obj.getId());
        });

        // 获取所有历史任务列表
        List<HistoricTaskInstanceEntity> historicTaskInstanceList = CommandContextUtil.getHistoricTaskService()
                .findHistoricTasksByProcessInstanceId(processInstanceId);

        historicTaskInstanceList.forEach(obj -> {
            if (deletedHisTaskMap.keySet().contains(obj.getId())) {
                deletedHisTaskMap.put(obj.getId(), obj);
            }
        });

//        // 移除正在执行的任务列表
//        taskEntityList.forEach(obj -> {
//            CommandContextUtil.getTaskService().deleteTasksByExecutionId(obj.getExecutionId());
//        });

        // 移除历史任务列表
        deletedHisTaskMap.forEach((k, v) -> {
            CommandContextUtil.getHistoricTaskService().deleteHistoricTask(v);
        });

    }

    /**
     * 创建新任务
     *
     * @param hisExecution
     * @param hisUserTask
     * @param variables
     */
    private void createNewExecution(Execution hisExecution, UserTask hisUserTask, Map<String, Object> variables) {
        hisUserTask.setAssignee(ASSIGNEE_EXPR);
        // 创建新任务
        ExecutionEntity executionEntity = (ExecutionEntity) hisExecution;

        // 创建新任务节点
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();

        // 创建新 execution
        ExecutionEntity newExecution;
        if (null == executionEntity.getParent()) {
            newExecution = executionEntityManager.createChildExecution(executionEntity);
        } else {
            newExecution = executionEntityManager.createChildExecution(executionEntity.getParent());
        }

        if (isEndElement){
            LOGGER.info("子流程已经完结，重新创建子流程");
            newExecution.setCurrentFlowElement(subProcessMap.get(currentSubProcessId));
            newExecution.setActive(true);
            newExecution = executionEntityManager.createChildExecution(newExecution);
        }

        newExecution.setCurrentFlowElement(hisUserTask);
        newExecution.setActive(true);
        // 测试设置变量
        newExecution.setVariablesLocal(variables);

        // 创建新任务
        CommandContextUtil.getAgenda().planContinueProcessInCompensation(newExecution);

        // 如果已经生成了下一节点任务，才需要执行此步骤
        if (hasNextTask && gatewayMap.size() > 0) {

            // 多级 gateway 处理 , 经过测试 多级网关只要不是并行 开头。都不会出问题
            if (gatewayMap.size() > 1) {
                LOGGER.warn("多级gateway");
            } else {
                ExecutionEntity finalNewExecution = newExecution;
                hisUserTask.getOutgoingFlows().forEach(obj -> {
                    Gateway gateway = gatewayMap.get(obj.getTargetRef());
                    if (null != gateway && gateway instanceof ParallelGateway) {
                        LOGGER.warn("并行网关任务");

                        List<Execution> list = runtimeService.createExecutionQuery()
                                .processInstanceId(hisExecution.getProcessInstanceId())
//                                .activityId(gateway.getId())
                                .list();

                        int gatewayExecutionSize = 0;

                        for (Execution execution : list) {
                            if (gateway.getId().equals(execution.getActivityId())) {
                                gatewayExecutionSize++;
                            }

                        }

                        if (gateway.getIncomingFlows().size() - gatewayExecutionSize > 1) {
                            LOGGER.warn("已经通过并行网关任务进行回退");
                            // 补全线条
                            int size = gateway.getIncomingFlows().size() - gatewayExecutionSize - 1;

                            for (int i = 0; i < size; i++) {

                                ExecutionEntity linkExecution = executionEntityManager.createChildExecution(finalNewExecution.getParent());
                                linkExecution.setActive(false);
                                linkExecution.setCurrentFlowElement(gateway);
                                CommandContextUtil.getAgenda().planContinueProcessInCompensation(linkExecution);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * @param bpmnModel
     * @param flowList
     * @param nextDefinitionKeySet
     */
    private void getNestUserTaskDefinitionKeySet(BpmnModel bpmnModel, List<SequenceFlow> flowList, Set<String> nextDefinitionKeySet) {

        for (SequenceFlow flow : flowList) {
            if (flow.getTargetFlowElement() instanceof UserTask) {
                nextDefinitionKeySet.add(flow.getTargetFlowElement().getId());

            } else if (flow.getTargetFlowElement() instanceof Gateway) {
                Gateway gateway = (Gateway) bpmnModel.getFlowElement(flow.getTargetFlowElement().getId());
                gatewayMap.put(gateway.getId(), gateway);
                getNestUserTaskDefinitionKeySet(bpmnModel, gateway.getOutgoingFlows(), nextDefinitionKeySet);

            } else if (flow.getTargetFlowElement() instanceof SubProcess) {
                SubProcess subProcess = (SubProcess) bpmnModel.getFlowElement(flow.getTargetRef());
                subProcess.getFlowElements().forEach(obj -> {
//                    nextDefinitionKeySet.add(obj.getId());
                    // 子流程 key 标识
                    subProcessDefinitionKeySet.add(obj.getId());
                });
            }
        }
    }

    private void getNestUserTaskDefinitionKeySet(BpmnModel bpmnModel, SubProcess subProcess, List<SequenceFlow> flowList, Set<String> nextDefinitionKeySet) {

        for (SequenceFlow flow : flowList) {
            if (flow.getTargetFlowElement() instanceof UserTask) {
                nextDefinitionKeySet.add(flow.getTargetFlowElement().getId());

            } else if (flow.getTargetFlowElement() instanceof Gateway) {
                Gateway gateway = (Gateway) subProcess.getFlowElement(flow.getTargetFlowElement().getId());
                gatewayMap.put(gateway.getId(), gateway);
                getNestUserTaskDefinitionKeySet(bpmnModel, subProcess, gateway.getOutgoingFlows(), nextDefinitionKeySet);
            } else if (flow.getTargetFlowElement() instanceof EndEvent) {
                getNestUserTaskDefinitionKeySet(bpmnModel, subProcess.getOutgoingFlows(), nextDefinitionKeySet);
                isEndElement = true;
            }
        }
    }
}
