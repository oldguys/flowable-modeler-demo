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
 * @ClassName: RollbackCompletedMultiInstanceTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/4/21 0021 下午 4:32
 * @Version：
 **/
@SpringBootTest
public class RollbackCompletedMultiInstanceTests extends CommonTaskTests {


    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;

    private String key = "rollback-multi-02";

    @Test
    public void testInit() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);

        Map<String, Object> variables = new HashMap<>();
        variables.put("a", "2");

        List<String> assigneeList = new ArrayList<>();
        assigneeList.add("a");
        assigneeList.add("b");
        assigneeList.add("c");

        variables.put("assigneeList", assigneeList);

        // complete a
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId()).list()
                .forEach(obj -> taskService.complete(obj.getId(), variables));

        // complete b-1
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId()).list()
                .forEach(obj -> taskService.complete(obj.getId(), variables));

//        // complete c-1
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId()).list()
                .forEach(obj -> taskService.complete(obj.getId(), variables));
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId()).list()
                .forEach(obj -> taskService.complete(obj.getId(), variables));
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId()).list()
                .forEach(obj -> taskService.complete(obj.getId(), variables));

        // d
        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId()).list()
                .forEach(obj -> printTask(obj));
    }

    @Test
    public void testRollback() {
        String taskId = "17ab5c1d-852d-11ea-ad2a-283a4d3b99a3";
        managementService.executeCommand(new RollbackCmd(taskId, "哈哈哈"));
    }


    @Test
    public void testComplete(){

//        String taskId = "594d3697-375d-11ea-b7c4-283a4d3b99a3";
        String taskId = "7addd72d-8509-11ea-a893-283a4d3b99a3";
        taskService.complete(taskId);

    }
}
