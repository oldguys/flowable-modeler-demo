package com.example.oldguy.examples.rollback;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: RollBackCommonTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/20 0020 下午 9:47
 * @Version：
 **/
public abstract class RollBackCommonTests {

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected ManagementService managementService;

    @Autowired
    protected HistoryService historyService;


    public List<Task> printProcessingTasks(String id) {

        List<Task> taskList = taskService.createTaskQuery().processInstanceId(id).list();

        System.out.println("代办任务列表=================================Start");
        taskList.forEach(obj -> {
            printTask(obj);
        });
        System.out.println("代办任务列表=================================End");

        return taskList;
    }

    public void completeAllTask(ProcessInstance pi, Map<String, Object> variables) {

        while (true) {

            List<Task> list = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();

            if (list.isEmpty()) {
                break;
            }

            list.forEach(obj -> {
                taskService.complete(obj.getId(), variables);
            });
        }


        System.out.println("历史任务列表========================Start");

        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(pi.getProcessInstanceId()).list();
        historicTaskInstances.forEach(obj -> {
            printTask(obj);
        });
        System.out.println("历史任务列表========================End");
        System.out.println("当前正在执行的流程实例 === ");

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(pi.getProcessInstanceId()).singleResult();

        System.out.println(processInstance);
    }


    public static void printTask(TaskInfo task) {
        System.out.println();
        System.out.println("taskId:" + task.getId());
        System.out.println("processInstanceId:" + task.getProcessInstanceId());
        System.out.println("task-name:" + task.getName());
        System.out.println("task-definition-key:" + task.getTaskDefinitionKey());
        System.out.println("task-assignee:" + task.getAssignee());
        System.out.println("task-createTime:" + task.getCreateTime());
        if (task instanceof HistoricTaskInstance) {
            System.out.println("task-endTime:" + HistoricTaskInstance.class.cast(task).getEndTime());
        }
        System.out.println();
    }

}
