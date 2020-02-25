package com.example.oldguy.modules.examples.controllers;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.examples.cmd.RollbackTaskCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.flowable.engine.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("回滚任务")
    @PostMapping("rollback")
    public CommonRsp rollback(@RequestParam String taskId, @RequestParam String assignee) {
        managementService.executeCommand(new RollbackTaskCommand(taskId, assignee));
        return CommonRsp.SUCCESS;
    }
}
