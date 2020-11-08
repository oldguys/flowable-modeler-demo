package com.example.oldguy.modules.flow.services.batchs;

import com.example.oldguy.modules.flow.services.batchs.commons.BatchTransactionFlag;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: CommonThreadExecutionService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 1:50
 * @Version：
 **/
public interface CommonThreadExecutionService {

    /**
     * 批处理
     *
     * @param threadExecution
     * @param sequence
     * @return
     */
    int executeBatch(ThreadExecution threadExecution, List sequence, Map<Long, TransactionStatus> longTransactionStatusMap, BatchTransactionFlag flag);
}
