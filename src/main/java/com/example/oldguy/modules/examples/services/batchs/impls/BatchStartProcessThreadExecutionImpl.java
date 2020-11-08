package com.example.oldguy.modules.examples.services.batchs.impls;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.examples.dto.batchs.BatchStartProcessInstanceItem;
import com.example.oldguy.modules.examples.dto.batchs.rsp.BatchStartProcessInstanceRsp;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName: BatchStartProcessThreadExecutionImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/30 0030 下午 2:23
 * @Version：
 **/
@Slf4j
public class BatchStartProcessThreadExecutionImpl implements ThreadExecution {

    private RuntimeService runtimeService;

    private List<BatchStartProcessInstanceRsp.ProcessInstanceItem> records;

    public BatchStartProcessThreadExecutionImpl(List<BatchStartProcessInstanceRsp.ProcessInstanceItem> records) {
        this.records = records;
        this.runtimeService = SpringContextUtils.getBean(RuntimeService.class);
    }

    @Override
    public void threadExecute(List list) {

        List<BatchStartProcessInstanceItem> itemList = list;
        itemList.forEach(obj -> {
            ProcessInstance pi = runtimeService.startProcessInstanceByKey(obj.getKey(), obj.getData());
            records.add(new BatchStartProcessInstanceRsp.ProcessInstanceItem(obj.getSequenceNo(), pi.getProcessInstanceId()));
        });
    }
}
