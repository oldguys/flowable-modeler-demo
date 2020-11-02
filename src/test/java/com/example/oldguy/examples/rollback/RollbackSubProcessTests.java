package com.example.oldguy.examples.rollback;

import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackCmd;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: RollbackSubProcessTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/4/7 0007 上午 10:13
 * @Version：
 **/
@SpringBootTest
public class RollbackSubProcessTests extends CommonTaskTests {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;

    String key = "test-rollback-subprocess";

    @Test
    public void test() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);

        System.out.println("pi:" + pi.getProcessInstanceId());

        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> taskService.complete(obj.getId()));



//        // 测试 嵌入式内部
//        Map<String, Object> variables = new HashMap<>();
//        List<String> assigneeList = new ArrayList<>();
//        assigneeList.add("a");
//        assigneeList.add("b");
//        assigneeList.add("c");
//
//        variables.put("assigneeList", assigneeList);
//
//        taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().get(0).getId(), variables);
//
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(System.out::println);
    }

    @Test
    public void testAssignee() {
//        taskService.addCandidateUser("472d281d-7d62-11ea-8a50-283a4d3b99a3","778");

        taskService.claim("472d281d-7d62-11ea-8a50-283a4d3b99a3", "998");
    }

    @Test
    public void testComplete() {

        String taskId = "ac87118a-7d9a-11ea-aa6e-283a4d3b99a3";
        Map<String, Object> variables = new HashMap<>();
        List<String> assigneeList = new ArrayList<>();
        assigneeList.add("a");
        assigneeList.add("b");
        assigneeList.add("c");

        variables.put("assigneeList", assigneeList);

        taskService.complete(taskId, variables);
    }

    @Test
    public void testRollback() {

        String taskId = "466c9db7-7f86-11ea-852b-283a4d3b99a3";
        managementService.executeCommand(new RollbackCmd(taskId, "嘻嘻嘻"));

    }
}
