package com.example.oldguy.examples.rollback;

import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackCmd;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName: RollbackCmdTests
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/25 0025 下午 4:23
 * @Version：
 **/
@SpringBootTest
public class RollbackCmdTests extends CommonTaskTests {

    @Autowired
    private ManagementService managementService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    String key = "test-rollback-normal-01";

    @Test
    public void testInit() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);
//        Task task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
        completeTask(pi.getProcessInstanceId(), 2);
        System.out.println("正在执行");
        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> printTask(obj));
        System.out.println("历史任务");
        historyService.createHistoricTaskInstanceQuery().processInstanceId(pi.getProcessInstanceId()).finished().list().forEach(System.out::println);

//        System.out.println("进行回滚");
//        managementService.executeCommand(new RollbackCmd(task.getId(), "abc"));
//        System.out.println("进行回滚后");
//        taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list().forEach(obj -> printTask(obj));
//        completeTask(pi.getProcessInstanceId(), 3);
    }

    @Test
    public void testRollback() {

        String hisTaskId = "b84c5221-7ed3-11ea-aff0-283a4d3b99a3"; // A
//        String hisTaskId = "b8572797-7ed3-11ea-aff0-283a4d3b99a3"; // B
//        Task[id=8645dbc3-7eca-11ea-9334-283a4d3b99a3, name=节点A]
//        Task[id=8651e9b9-7eca-11ea-9334-283a4d3b99a3, name=节点B]
        managementService.executeCommand(new RollbackCmd(hisTaskId, "abc"));
    }

}
