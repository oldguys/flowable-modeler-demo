package com.example.oldguy.examples.functions;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: VariableFunctionTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/19 0019 下午 10:17
 * @Version：
 **/
@SpringBootTest
public class VariableFunctionTests {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    private String key = "test-expr-query";

    @Test
    public void test() {

        Map<String, Object> variables = new HashMap<>();
        variables.put("a",1);
        variables.put("b",1);
        variables.put("c",2);
        variables.put("d",11);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, variables);

        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> printTask(obj));
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
