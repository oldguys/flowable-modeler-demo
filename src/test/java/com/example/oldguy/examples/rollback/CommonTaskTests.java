package com.example.oldguy.examples.rollback;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: CommonTaskTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/16 0016 下午 2:41
 * @Version：
 **/
public abstract class CommonTaskTests {

    @Autowired
    private TaskService taskService;

    public void completeTask(String processInstanceId, int count) {

        int index = 0;

        Map<String, Object> variables = new HashMap<>();
//        variables.put("key", 1);
        variables.put("a", 1);
//        variables.put("a", "1");
        while (true) {

            List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            taskList.forEach(obj -> {
                taskService.complete(obj.getId(), variables);
            });

            if (taskList.isEmpty()) {
                break;
            }

            index++;

            if (index == count) {
                break;
            }
        }
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
