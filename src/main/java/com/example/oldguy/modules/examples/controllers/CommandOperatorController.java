package com.example.oldguy.modules.examples.controllers;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.flow.services.cmd.rollback.RollbackCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.flowable.engine.ManagementService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName: CommandOperatorController
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/19 0019 下午 9:11
 * @Version：
 **/
@Api(tags = "任务操作")
@RequestMapping("command")
@RestController
public class CommandOperatorController {


    @Autowired
    private ManagementService managementService;
    @Autowired
    private TaskService taskService;

    @ApiOperation("完成任务")
    @PostMapping("runtime/{taskId}/complete")
    public CommonRsp completeTask(@PathVariable String taskId) {
        taskService.complete(taskId);
        return CommonRsp.SUCCESS;
    }


    @ApiOperation("回滚任务")
    @PostMapping("rollback")
    public CommonRsp rollback(@RequestParam String taskId, @RequestParam String assignee) {
        managementService.executeCommand(new RollbackCmd(taskId, assignee));
        return CommonRsp.SUCCESS;
    }

}
