package com.example.oldguy.modules.examples.services.batchs.impls;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.app.services.FlowTaskService;
import com.example.oldguy.modules.examples.dto.batchs.BatchCompleteTaskItem;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchCompleteTaskRsp;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @ClassName: BatchTaskCompleteTaskWithSingleTransactionThreadExecutionImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 5:17
 * @Version：
 **/
@Slf4j
public class BatchTaskCompleteTaskWithSingleTransactionThreadExecutionImpl implements ThreadExecution {

    private List<BatchCompleteTaskRsp.CompleteTaskItem> result;

    private FlowTaskService flowTaskService;

    public BatchTaskCompleteTaskWithSingleTransactionThreadExecutionImpl(List<BatchCompleteTaskRsp.CompleteTaskItem> result) {
        this.result = result;
        this.flowTaskService = SpringContextUtils.getBean(FlowTaskService.class);
    }

    @Override
    public void threadExecute(List list) {
        List<BatchCompleteTaskItem> itemList = list;
        flowTaskService.completeTask(itemList);
    }
}
