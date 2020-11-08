package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.modules.flow.services.batchs.CommonThreadExecutionService;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName: DefaultCommonThreadExecutionServiceImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 1:50
 * @Version：
 **/
@Slf4j
@Service
public class DefaultCommonThreadExecutionServiceBean implements CommonThreadExecutionService {

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Override
//    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public int executeBatch(ThreadExecution threadExecution, List sequence, List<TransactionStatus> transactionStatuses, BatchTransactionFlag flag) {

//        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
//        TransactionStatus transactionStatus = transactionManager.getTransaction(definition);
        synchronized (flag){

            TransactionStatus transactionStatus = transactionManager.getTransaction(TransactionDefinition.withDefaults());
            transactionStatuses.add(transactionStatus);
            flag.getCompleteThreads().incrementAndGet();
        }

        try {
            threadExecution.threadExecute(sequence);
        } finally {
            log.info("完成任务：" + Thread.currentThread().getName());
        }
//        transactionManager.commit(transactionStatus);

        return 0;
    }
}
