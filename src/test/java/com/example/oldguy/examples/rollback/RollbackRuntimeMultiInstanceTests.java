package com.example.oldguy.examples.rollback;

import com.example.oldguy.modules.examples.cmd.rollback.RollbackCmd;
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
 * @ClassName: RollbackRuntimeMultiInstanceTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/31 0031 下午 5:54
 * @Version：
 **/
@SpringBootTest
public class RollbackRuntimeMultiInstanceTests extends CommonTaskTests {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;

    String key = "test-rollback-multi01";

    @Test
    public void testSequentialInstance() {

        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 2);

        List<String> assigneeList = new ArrayList<>();
        assigneeList.add("a");
        assigneeList.add("b");
        assigneeList.add("c");
        assigneeList.add("d");
        variables.put("assigneeList", assigneeList);

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, variables);
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> printTask(obj));
    }

    @Test
    public void testMultiInstance() {

        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 1);

        List<String> assigneeList = new ArrayList<>();
        assigneeList.add("a");
        assigneeList.add("b");
        assigneeList.add("c");
        assigneeList.add("d");
        variables.put("assigneeList", assigneeList);

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key, variables);
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> printTask(obj));
    }

    @Test
    public void testRollback(){
        String taskId = "576276cf-7685-11ea-b6b7-283a4d3b99a3";
        managementService.executeCommand(new RollbackCmd(taskId,"哈哈123456"));
    }
}
