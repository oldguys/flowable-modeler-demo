package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.services.batchs.CommonThreadExecutionService;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: DefaultPoolTask
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/29 0029 下午 2:44
 * @Version：
 **/
public class SingleTransactionPoolTask implements Runnable {

    private final ThreadExecution threadExecution;

    private final List<?> list;

    private final BatchTransactionFlag flag;

    public SingleTransactionPoolTask(ThreadExecution threadExecution, List<?> list, BatchTransactionFlag flag) {
        this.threadExecution = threadExecution;
        this.list = list;
        this.flag = flag;
    }

    @Override
    public void run() {
        try {
            threadExecution.threadExecute(list);
        } finally {
            flag.getCompleteThreads().incrementAndGet();
        }
    }
}
