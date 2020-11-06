package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.modules.flow.exceptions.FlowRuntimeException;
import com.example.oldguy.modules.flow.services.batchs.CommonThreadExecutionService;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @ClassName: DefaultCommonThreadExecutionServiceImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 1:50
 * @Version：
 **/
@Service
public class DefaultCommonThreadExecutionServiceBean implements CommonThreadExecutionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeBatch(ThreadExecution threadExecution, List<?> sequence, BatchTransactionFlag flag, UUID threadId) {

        try {
            threadExecution.threadExecute(sequence);
        } catch (Exception e) {
            e.printStackTrace();
            flag.getErrorThreadIds().add(threadId);
        }
        flag.waitForEnd();
        if (!flag.getErrorThreadIds().isEmpty()) {
            throw new FlowRuntimeException("子线程异常，所有事务回滚：" + flag.getErrorThreadIds());
        }

        return 0;
    }
}
