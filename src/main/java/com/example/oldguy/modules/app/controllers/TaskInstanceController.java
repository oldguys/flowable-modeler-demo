package com.example.oldguy.modules.app.controllers;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.app.dto.rsp.TaskRsp;
import com.example.oldguy.modules.app.services.TaskInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName: TaskController
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/11 0011 下午 9:13
 * @Version：
 **/
@Api(tags = "任务实例")
@RestController
@RequestMapping("task")
public class TaskInstanceController {

    @Autowired
    private TaskInstanceService taskInstanceService;

    @ApiOperation("获取任务列表")
    @GetMapping("runtime/list")
    public CommonRsp<List<TaskRsp>> getTaskRspList(String assignee) {
        return new CommonRsp<>(taskInstanceService.getTaskList(assignee));
    }


}
