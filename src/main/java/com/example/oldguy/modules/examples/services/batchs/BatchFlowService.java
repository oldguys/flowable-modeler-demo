package com.example.oldguy.modules.examples.services.batchs;

import com.example.oldguy.modules.examples.dto.batchs.req.BatchCompleteTaskReq;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchStartProcessInstanceReq;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchCompleteTaskRsp;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchStartProcessInstanceRsp;

/**
 * @ClassName: BatchFlowService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/30 0030 下午 2:28
 * @Version：
 **/
public interface BatchFlowService {

    /**
     *  批量开启流程实例
     * @param req
     * @return
     */
    BatchStartProcessInstanceRsp batchStartProcessInstance(BatchStartProcessInstanceReq req);

    /**
     *  批量完成任务接口
     * @param req
     * @return
     */
    BatchCompleteTaskRsp batchCompleteTasks(BatchCompleteTaskReq req);
}
