package com.example.oldguy.examples.rollback;

import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackCmd;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName: RollbackCallActivityTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/4/14 0014 下午 2:03
 * @Version：
 **/
@SpringBootTest
public class RollbackCallActivityTests extends CommonTaskTests {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;

    String key = "rollback-callActivity";

    @Test
    public void init() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);

        System.out.println("pi:" + pi.getProcessInstanceId());

        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(System.out::println);
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> taskService.complete(obj.getId()));
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> taskService.complete(obj.getId()));
        System.out.println("测试");
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(System.out::println);
    }

    @Test
    public void toTest() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);

        System.out.println("pi:" + pi.getProcessInstanceId());


        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(System.out::println);
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> taskService.complete(obj.getId()));

        String taskId = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult().getId();
        taskService.complete(taskId);
        System.out.println("taskId:" + taskId);
        System.out.println("测试====Start");
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(System.out::println);
        System.out.println("测试====End");

        managementService.executeCommand(new RollbackCmd(taskId, "嘻嘻嘻"));
    }

    @Test
    public void test() {
//        String taskId = "46a58259-7f29-11ea-ae11-283a4d3b99a3";
        String taskId = "5deb9d12-8253-11ea-8632-283a4d3b99a3";

        managementService.executeCommand(new RollbackCmd(taskId, "嘻嘻嘻:c-2"));
    }


}
