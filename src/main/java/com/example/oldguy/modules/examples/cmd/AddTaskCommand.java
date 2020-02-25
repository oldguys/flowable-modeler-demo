//package com.example.oldguy.modules.examples.cmd;
//
//import com.example.oldguy.common.utils.SpringContextUtils;
//import com.example.oldguy.modules.app.constants.AddTaskConstants;
//import com.example.oldguy.modules.app.dao.entities.SpecialTaskInstance;
//import com.example.oldguy.modules.app.dao.jpas.SpecialTaskInstanceMapper;
//import com.example.oldguy.modules.app.exceptions.FlowableRuntimeException;
//import com.example.oldguy.modules.app.services.SpecialTaskInstanceService;
//import org.flowable.bpmn.model.BpmnModel;
//import org.flowable.bpmn.model.Process;
//import org.flowable.bpmn.model.UserTask;
//import org.flowable.common.engine.impl.interceptor.CommandContext;
//import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
//import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
//import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
//import org.flowable.engine.impl.util.CommandContextUtil;
//import org.flowable.engine.repository.ProcessDefinition;
//import org.flowable.engine.runtime.ProcessInstance;
//import org.flowable.task.api.Task;
//import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
//import org.json.JSONArray;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Date;
//import java.util.List;
//
///**
// * @ClassName: AddTaskCommand
// * @Author: huangrenhao
// * @Description: 加签：普通任务转会签，
// * @CreateTime： 2020/2/11 0011 下午 9:11
// * @Version：
// **/
//public class AddTaskCommand extends CommonCommand<String> {
//
//    private static Logger LOGGER = LoggerFactory.getLogger(AddTaskCommand.class);
//
//
//    private String taskId;
//    /**
//     * 任务执行人列表
//     */
//    private List<String> assignees;
//
//    private String currentAssignee;
//
//    private SpecialTaskInstanceMapper mapper;
//
//    private SpecialTaskInstanceService specialTaskInstanceService;
//
//    public AddTaskCommand(String taskId, List<String> assignees, String currentAssignee) {
//        this.taskId = taskId;
//        this.assignees = assignees;
//        this.currentAssignee = currentAssignee;
//        mapper = SpringContextUtils.getBean(SpecialTaskInstanceMapper.class);
//        specialTaskInstanceService = SpringContextUtils.getBean(SpecialTaskInstanceService.class);
//    }
//
//
//    @Override
//    public String execute(CommandContext commandContext) {
//
//        TaskEntityImpl task = (TaskEntityImpl) taskService.createTaskQuery().taskId(taskId).singleResult();
//        if (null == task) {
//            String msg = "无效 [ taskId = " + taskId + " ] ";
//            LOGGER.error(msg);
//            throw new FlowableRuntimeException(msg);
//        }
//        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
//        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
//
//        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
//        Process process = bpmnModel.getProcesses().get(0);
//
//        UserTask userTask = (UserTask) process.getFlowElement(task.getTaskDefinitionKey());
//
//        if (userTask.getLoopCharacteristics() != null) {
//            String msg = "当前节点是会签任务,不符合普通任务线性加签!";
//            LOGGER.error(msg);
//            throw new FlowableRuntimeException(msg);
//        }
//
//
//        // 当前执行对象
//        ExecutionEntityImpl execution = (ExecutionEntityImpl) runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
//
//        // 创建缓存记录
//        SpecialTaskInstance entity = getSpecialTaskInstance(task, processInstance, processDefinition);
//
//
//        int currentIndex = assignees.indexOf(currentAssignee);
//        if (currentIndex == -1) {
//            String msg = "参数错误,任务列表不存在指定 任务执行人 !";
//            LOGGER.error(msg);
//            throw new FlowableRuntimeException(msg);
//        }
//
//
//        /**
//         *  创建任务
//         */
//        if (null == entity.getId()) {
//
//            LOGGER.info("一次加签");
//
//            // 将节点转换为会签节点
//            specialTaskInstanceService.trainToMultiInstanceTask(userTask);
//
//            ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
//
////            ExecutionEntity multiTaskExecution = executionEntityManager.createChildExecution(execution.getParent());
////            ExecutionEntity newExecution = executionEntityManager.createChildExecution(multiTaskExecution);
//
//            ExecutionEntity newExecution = executionEntityManager.createChildExecution(execution);
//
//            // 配置会报错
////            multiTaskExecution.setCurrentFlowElement(userTask);
//
//            newExecution.setActive(true);
//            newExecution.setVariableLocal(AddTaskConstants.LOOP_COUNTER, currentIndex);
//            newExecution.setVariableLocal(AddTaskConstants.ASSIGNEE_USER, currentAssignee);
//            newExecution.setCurrentFlowElement(userTask);
//            newExecution.setRevision(currentIndex);
//
//            // 测试变量
//            newExecution.getParent().setVariableLocal(AddTaskConstants.NUMBER_OF_INSTANCES, assignees.size());
//            newExecution.getParent().setVariableLocal(AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES, assignees.size() - currentIndex);
//            newExecution.getParent().setVariableLocal(AddTaskConstants.NUMBER_OF_COMPLETED_INSTANCES, currentIndex);
//
//
//            // 持久化 任务ID
////            entity.setExecutionId(multiTaskExecution.getId());
//
//
////             TODO 移除任务
////            executionEntityManager.delete
//            CommandContextUtil.getTaskService().deleteTasksByExecutionId(execution.getId());
////            executionEntityManager.delete(execution.getId());
////            taskService.deleteTasksByExecutionId(execution.getId());
//
//
//            // 进行任务队列
//            CommandContextUtil.getAgenda().planContinueProcessInCompensation(newExecution);
//
//            // TODO 使用父级别
//            runtimeService.setVariable(execution.getParent().getId(), AddTaskConstants.ASSIGNEE_LIST, assignees);
//
////            runtimeService.setVariableLocal(execution.getId(), AddTaskConstants.NUMBER_OF_INSTANCES, assignees.size());
////            runtimeService.setVariableLocal(execution.getId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES, assignees.size() - currentIndex);
////            runtimeService.setVariableLocal(execution.getId(), AddTaskConstants.NUMBER_OF_COMPLETED_INSTANCES, currentIndex);
//
////            runtimeService.setVariableLocal(newExecution.getId(), AddTaskConstants.LOOP_COUNTER, currentIndex);
////            runtimeService.setVariableLocal(newExecution.getId(), AddTaskConstants.ASSIGNEE_USER, currentAssignee);
//
//            System.out.println("execution-1:" + execution.getParent().getId());
//            System.out.println("execution-2:" + execution.getId());
//            System.out.println("execution-3:" + newExecution.getId());
//
//            // 配置会报错
////            CommandContextUtil.getAgenda().planContinueProcessInCompensation(multiTaskExecution);
//
//
//            mapper.insert(entity);
//
//        } else {
//
//            LOGGER.info("二次加签");
//
//            runtimeService.setVariable(execution.getParent().getId(), AddTaskConstants.ASSIGNEE_LIST, assignees);
//
//            runtimeService.setVariableLocal(execution.getParent().getId(), AddTaskConstants.NUMBER_OF_INSTANCES, assignees.size());
//            runtimeService.setVariableLocal(execution.getParent().getId(), AddTaskConstants.NUMBER_OF_ACTIVE_INSTANCES, assignees.size() - currentIndex);
//            runtimeService.setVariableLocal(execution.getParent().getId(), AddTaskConstants.NUMBER_OF_COMPLETED_INSTANCES, currentIndex);
//
//            runtimeService.setVariableLocal(execution.getId(), AddTaskConstants.LOOP_COUNTER, currentIndex);
//            runtimeService.setVariableLocal(execution.getId(), AddTaskConstants.ASSIGNEE_USER, currentAssignee);
//
//            taskService.setAssignee(taskId, currentAssignee);
//
//            mapper.updateById(entity);
//        }
//
//
//        return "加签成功!";
//    }
//
//
//    /**
//     * 创建实体
//     *
//     * @param task
//     * @param processInstance
//     * @param processDefinition
//     */
//    private SpecialTaskInstance getSpecialTaskInstance(Task task, ProcessInstance processInstance, ProcessDefinition processDefinition) {
//
//        SpecialTaskInstance entity = specialTaskInstanceService.getOne(task.getProcessInstanceId(), task.getTaskDefinitionKey());
//
//        if (null == entity) {
//            entity = new SpecialTaskInstance();
//            entity.setTaskDefinitionKey(task.getTaskDefinitionKey());
//            entity.setProcessInstanceId(processInstance.getProcessInstanceId());
//            entity.setProcessDefinitionId(processDefinition.getId());
//            entity.setType(SpecialTaskInstance.SpecialTaskInstanceType.ADD_TASK.getCode());
//            entity.setCreateTime(new Date());
//        }
//
//        JSONArray array = new JSONArray(assignees);
//        String data = array.toString();
//        entity.setData(data);
//        entity.setLastUpdateTime(new Date());
//
//
//        return entity;
//    }
//
//}
