package com.example.oldguy.modules.flow.services.batchs;

import com.example.oldguy.modules.flow.services.batchs.commons.BatchTransactionFlag;

import java.util.List;
import java.util.UUID;

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
     * @param threadExecution
     * @param sequence
     * @param flag
     * @return
     */
    int executeBatch(ThreadExecution threadExecution, List<?> sequence, BatchTransactionFlag flag, UUID threadId);
}
