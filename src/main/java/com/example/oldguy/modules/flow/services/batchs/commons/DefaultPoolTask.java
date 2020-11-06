package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.services.batchs.CommonThreadExecutionService;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;

import java.util.List;
import java.util.UUID;

/**
 * @ClassName: DefaultPoolTask
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/29 0029 下午 2:44
 * @Version：
 **/
public class DefaultPoolTask implements Runnable {

    private ThreadExecution threadExecution;

    private List<?> list;

    private CommonThreadExecutionService commonThreadExecutionService;

    private UUID threadId;

    private BatchTransactionFlag flag;

    public DefaultPoolTask(ThreadExecution threadExecution, List<?> list, BatchTransactionFlag flag) {
        this.threadExecution = threadExecution;
        this.list = list;
        this.commonThreadExecutionService = SpringContextUtils.getBean(CommonThreadExecutionService.class);
        this.threadId = UUID.randomUUID();
        this.flag = flag;
    }

    @Override
    public void run() {
        commonThreadExecutionService.executeBatch(threadExecution, list, flag, threadId);
    }
}
