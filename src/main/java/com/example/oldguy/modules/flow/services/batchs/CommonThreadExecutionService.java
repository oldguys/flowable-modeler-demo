package com.example.oldguy.modules.flow.services.batchs;

import java.util.List;

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
     * @return
     */
    int executeBatch(ThreadExecution threadExecution, List sequence);
}
