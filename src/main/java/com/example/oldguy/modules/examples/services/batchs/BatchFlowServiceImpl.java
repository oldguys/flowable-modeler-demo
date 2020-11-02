package com.example.oldguy.modules.examples.services.batchs;

import com.example.oldguy.modules.examples.dto.batchs.StartProcessInstanceItem;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchStartProcessInstanceReq;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchStartProcessInstanceRsp;
import com.example.oldguy.modules.examples.services.batchs.impls.BatchStartProcessThreadExecutionImpl;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import com.example.oldguy.modules.flow.services.batchs.commons.FlowThreadPoolExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @ClassName: BatchFlowServiceImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/30 0030 下午 2:26
 * @Version：
 **/
@Service
public class BatchFlowServiceImpl implements BatchFlowService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchStartProcessInstanceRsp batchStartProcessInstance(BatchStartProcessInstanceReq req) {

        List<StartProcessInstanceItem> toDoSequence = new ArrayList<>();

        req.getItemList().stream()
                .forEach(obj -> obj.getSequence().forEach(
                        item -> toDoSequence.add(new StartProcessInstanceItem(item, obj.getKey(), obj.getData())
                        )
                ));

        List<BatchStartProcessInstanceRsp.ProcessInstanceItem> result = new Vector<>();
        ThreadExecution threadExecution = new BatchStartProcessThreadExecutionImpl(result);
        FlowThreadPoolExecutor.executeTask(threadExecution, toDoSequence, 100);

        return new BatchStartProcessInstanceRsp(result);
    }
}
