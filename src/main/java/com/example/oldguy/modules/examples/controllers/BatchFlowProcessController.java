package com.example.oldguy.modules.examples.controllers;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchCompleteTaskReq;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchStartProcessInstanceReq;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchCompleteTaskRsp;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchStartProcessInstanceRsp;
import com.example.oldguy.modules.examples.services.batchs.BatchFlowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @ClassName: BatchFlowProcessController
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/30 0030 下午 3:33
 * @Version：
 **/
@Api(tags = "批量流程处理")
@RestController
@RequestMapping("batch/process")
public class BatchFlowProcessController {

    @Autowired
    private BatchFlowService batchFlowService;

    @ApiOperation("批量发起流程")
    @PostMapping("instance/start")
    public CommonRsp<BatchStartProcessInstanceRsp> batchStartProcessInstance(@RequestBody BatchStartProcessInstanceReq req){
        return new CommonRsp<>(batchFlowService.batchStartProcessInstance(req));
    }

    @ApiOperation("批量发起流程")
    @PostMapping("task/complete")
    public CommonRsp<BatchCompleteTaskRsp> batchCompleteTasks(@RequestBody BatchCompleteTaskReq req){
        return new CommonRsp<>(batchFlowService.batchCompleteTasks(req));
    }

}
