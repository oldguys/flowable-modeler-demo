package com.example.oldguy.examples.rollback;

import com.example.oldguy.modules.examples.cmd.RollbackTaskCommand;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class RollBackGatewayTests extends RollBackCommonTests {

    private String key = "rollback-gateway-01";


    @Test
    public void testRollBack_B() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);
        Map<String, Object> variables = new HashMap<>();
        variables.put("a", "2");

        Task task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        printTask(task);

        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        printTask(task);

        String hisTaskId = task.getId();
        taskService.complete(task.getId(), variables);
        // 查看正在执行任务列表
        printProcessingTasks(pi.getId());

        System.out.println("回滚任务=========================");
        managementService.executeCommand(new RollbackTaskCommand(hisTaskId, "abc"));
        System.out.println("回滚任务=========================");

        // 查看正在执行任务列表
        printProcessingTasks(pi.getId());


        // 完成任务
        completeAllTask(pi, variables);
    }

    @Test
    public void testRollBack_A() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);
        Map<String, Object> variables = new HashMap<>();
        variables.put("a", "2");

        Task task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        printTask(task);

        String hisTaskId = task.getId();
        taskService.complete(task.getId());

        // 执行任务 B
//        task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
//        printTask(task);
//        taskService.complete(task.getId(), variables);
//


        // 查看正在执行任务列表
        printProcessingTasks(pi.getId());

        System.out.println("回滚任务=========================");
        managementService.executeCommand(new RollbackTaskCommand(hisTaskId, "abc"));
        System.out.println("回滚任务=========================");

        // 查看正在执行任务列表
        printProcessingTasks(pi.getId());


        // 完成任务
        completeAllTask(pi, variables);
    }


}
