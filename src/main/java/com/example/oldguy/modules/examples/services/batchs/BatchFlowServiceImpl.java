package com.example.oldguy.modules.examples.services.batchs;

import com.example.oldguy.modules.examples.dto.batchs.BatchCompleteTaskItem;
import com.example.oldguy.modules.examples.dto.batchs.BatchStartProcessInstanceItem;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchCompleteTaskReq;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchStartProcessInstanceReq;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchCompleteTaskRsp;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchStartProcessInstanceRsp;
import com.example.oldguy.modules.examples.services.batchs.impls.BatchStartProcessThreadExecutionImpl;
import com.example.oldguy.modules.examples.services.batchs.impls.BatchTaskCompleteTaskWithSingleTransactionThreadExecutionImpl;
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

        List<BatchStartProcessInstanceItem> toDoSequence = new ArrayList<>();

        req.getItemList().stream()
                .forEach(obj -> obj.getSequence().forEach(
                        item -> toDoSequence.add(new BatchStartProcessInstanceItem(item, obj.getKey(), obj.getData())
                        )
                ));

        List<BatchStartProcessInstanceRsp.ProcessInstanceItem> result = new Vector<>();
        ThreadExecution threadExecution = new BatchStartProcessThreadExecutionImpl(result);
        FlowThreadPoolExecutor.executeTask(threadExecution, toDoSequence, 100);

        return new BatchStartProcessInstanceRsp(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchCompleteTaskRsp batchCompleteTasks(BatchCompleteTaskReq req) {

        List<BatchCompleteTaskItem> toDoSequence = new ArrayList<>();

        req.getItemList().forEach(obj ->
            obj.getTaskIds().forEach(item -> toDoSequence.add(new BatchCompleteTaskItem(item, obj.getData())))
        );

        List<BatchCompleteTaskRsp.CompleteTaskItem> result = new Vector<>();
        ThreadExecution threadExecution = new BatchTaskCompleteTaskWithSingleTransactionThreadExecutionImpl(result);
        FlowThreadPoolExecutor.executeTask(threadExecution, toDoSequence, 100);


        return new BatchCompleteTaskRsp(result);
    }
}
