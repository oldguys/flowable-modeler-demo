package com.example.oldguy.modules.app.controllers;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.app.dto.rsp.ProcessInstanceRsp;
import com.example.oldguy.modules.app.services.ProcessInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName: ProcessInstanceController
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/15 0015 上午 8:31
 * @Version：
 **/
@Api(tags = "流程实例管理")
@RestController
@RequestMapping("processInstance")
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @ApiOperation("获取当前正在执行的流程实例列表")
    @GetMapping("runtime/list")
    public CommonRsp<List<ProcessInstanceRsp>> getRuntimeProcessInstanceList(){
        return new CommonRsp<>(processInstanceService.getRuntimeProcessInstanceList());
    }
}
