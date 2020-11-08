package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.services.batchs.CommonThreadExecutionService;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @ClassName: DefaultPoolTask
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/29 0029 下午 2:44
 * @Version：
 **/
public class BatchTransactionPoolTask implements Runnable {

    private final ThreadExecution threadExecution;

    private final List<?> list;

    private final CommonThreadExecutionService commonThreadExecutionService;

    private final Map<Long, TransactionStatus> longTransactionStatusMap;

    private final BatchTransactionFlag flag;

    public BatchTransactionPoolTask(ThreadExecution threadExecution, List<?> list, Map<Long, TransactionStatus> longTransactionStatusMap, BatchTransactionFlag flag) {
        this.threadExecution = threadExecution;
        this.list = list;
        this.longTransactionStatusMap = longTransactionStatusMap;
        this.commonThreadExecutionService = SpringContextUtils.getBean(CommonThreadExecutionService.class);
        this.flag = flag;
    }

    @Override
    public void run() {
        commonThreadExecutionService.executeBatch(threadExecution, list, longTransactionStatusMap, flag);
    }
}
