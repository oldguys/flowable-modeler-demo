package com.example.oldguy.modules.examples.services.batchs;

import com.example.oldguy.modules.examples.dto.batchs.BatchCompleteTaskItem;
import com.example.oldguy.modules.examples.dto.batchs.BatchStartProcessInstanceItem;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchCompleteTaskReq;
import com.example.oldguy.modules.examples.dto.batchs.req.BatchStartProcessInstanceReq;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchCompleteTaskRsp;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchStartProcessInstanceRsp;
import com.example.oldguy.modules.examples.services.batchs.impls.BatchStartProcessThreadExecutionImpl;
import com.example.oldguy.modules.examples.services.batchs.impls.BatchTaskCompleteTaskWithBatchTransactionThreadExecutionImpl;
import com.example.oldguy.modules.examples.services.batchs.impls.BatchTaskCompleteTaskWithSingleTransactionThreadExecutionImpl;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import com.example.oldguy.modules.flow.services.batchs.commons.FlowThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class BatchFlowServiceImpl implements BatchFlowService {


    @Override
//    @Transactional(rollbackFor = Exception.class)
    public BatchStartProcessInstanceRsp batchStartProcessInstance(BatchStartProcessInstanceReq req) {

        List<BatchStartProcessInstanceItem> toDoSequence = new ArrayList<>();

        req.getItemList().stream()
                .forEach(obj -> obj.getSequence().forEach(
                        item -> toDoSequence.add(new BatchStartProcessInstanceItem(item, obj.getKey(), obj.getData())
                        )
                ));

        List<BatchStartProcessInstanceRsp.ProcessInstanceItem> result = new Vector<>();
        try {
            ThreadExecution threadExecution = new BatchStartProcessThreadExecutionImpl(result);
            FlowThreadPoolExecutor.executeTask(threadExecution, toDoSequence, 100);
        }catch (Exception e){
            log.info("顶级方法");
            e.printStackTrace();
            throw e;
        }


        return new BatchStartProcessInstanceRsp(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchCompleteTaskRsp batchCompleteTasks(BatchCompleteTaskReq req) {

        List<BatchCompleteTaskItem> toDoSequence = new ArrayList<>();

        req.getItemList().forEach(obj ->
                obj.getTaskIds().forEach(item -> toDoSequence.add(new BatchCompleteTaskItem(item, obj.getComment(), obj.getData())))
        );

        List<BatchCompleteTaskRsp.CompleteTaskItem> result = new Vector<>();
        ThreadExecution threadExecution;
        if (req.getTransactionalBatchIs()) {
            threadExecution = new BatchTaskCompleteTaskWithBatchTransactionThreadExecutionImpl(result);
        } else {
            threadExecution = new BatchTaskCompleteTaskWithSingleTransactionThreadExecutionImpl(result);
        }

        log.info("threadExecution:" + threadExecution.getClass());
        FlowThreadPoolExecutor.executeTask(threadExecution, toDoSequence, 5);

        return new BatchCompleteTaskRsp(result);
    }
}
