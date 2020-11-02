package com.example.oldguy.examples.rollback;

import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackCmd;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: RollbackGateWayTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/4/16 0016 上午 11:46
 * @Version：
 **/
@SpringBootTest
public class RollbackGateWayTests extends CommonTaskTests {

    private String key02 = "rollback-gateway-02";
    private String key03 = "test-rollback-gateway03";

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;

    @Test
    public void testRollbackGateway03(){

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key03);

        // complete a
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> taskService.complete(obj.getId()));

        // complete b
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> taskService.complete(obj.getId()));

        // complete c
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> taskService.complete(obj.getId()));

        // d
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> printTask(obj));
    }

    /**
     *
     */
    @Test
    public void testRollbackGateway02() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key02);

        System.out.println("pi:" + pi.getProcessInstanceId());
        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 2);
        variables.put("b", "1");
        variables.put("d", "2");
        variables.put("c", "2");

        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> taskService.complete(obj.getId(), variables));

        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> taskService.complete(obj.getId(), variables));

        System.out.println("当前任务");
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> printTask(obj));

        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list();

        list.forEach(task -> taskService.complete(task.getId()));

//        taskService.complete(list.get(0).getId());
//        taskService.complete(list.get(1).getId());
    }

    @Test
    public void testInit() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key02);

        System.out.println("pi:" + pi.getProcessInstanceId());
        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 2);

        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> taskService.complete(obj.getId(), variables));

    }

    @Test
    public void testComplete() {

        String processInstanceId = "b763a46b-806e-11ea-bc0d-283a4d3b99a3";
        Map<String, Object> variables = new HashMap<>();
        variables.put("b", "1");
        variables.put("d", "2");
        variables.put("c", "2");

        taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list().forEach(obj -> taskService.complete(obj.getId(), variables));

    }


    @Test
    public void testRollback() {
        String taskId = "b5ee8053-8375-11ea-a3ac-283a4d3b99a3";

        managementService.executeCommand(new RollbackCmd(taskId, "嘻嘻嘻-测试01"));
    }

}
