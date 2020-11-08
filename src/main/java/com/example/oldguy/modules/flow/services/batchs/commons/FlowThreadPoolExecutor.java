package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.common.dto.ErrorCode;
import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.exceptions.FlowRuntimeException;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.javax.el.PropertyNotFoundException;
import org.flowable.ui.common.security.SecurityUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * @ClassName: DefaultFlowThreadPoolExecutorImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/28 0028 上午 10:23
 * @Version：
 **/

@Slf4j
public class FlowThreadPoolExecutor {

    public static boolean executeTask(ThreadExecution execution, List toDoList, int groupSize) {
        int corePoolSize = 10;
        int maximumPoolSize = 10;
        long keepAliveTime = 3;
        return executeTask(corePoolSize, maximumPoolSize, keepAliveTime, execution, toDoList, groupSize);
    }

    public static boolean executeTask(int corePoolSize,
                                      int maximumPoolSize,
                                      long keepAliveTime,
                                      ThreadExecution execution,
                                      List toDoList,
                                      int groupSize
    ) {
        if (toDoList.isEmpty()) {
            log.warn("空任务组,不需要进行处理");
            return false;
        }

        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("flow-pool-%d")
                .build();


        String currentUserId = SecurityUtils.getCurrentUserId();

        ThreadPoolExecutor executorService = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                namedThreadFactory) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                Authentication.setAuthenticatedUserId(currentUserId);
            }
        };


        int group = toDoList.size() / groupSize;
        if (toDoList.size() % groupSize > 0) {
            group += 1;
        }

        // 事务集合
        List<TransactionStatus> transactionStatuses = new Vector<>();
        List<Future<?>> futures = new ArrayList<>();

        BatchTransactionFlag flag = new BatchTransactionFlag(group);

        for (int i = 0; i < group; i++) {

            int startIndex = i * groupSize;
            int endIndex = (i + 1) * groupSize;
            if (endIndex > toDoList.size()) {
                endIndex = toDoList.size();
            }
            List<?> items = toDoList.subList(startIndex, endIndex);

            futures.add(executorService.submit(new DefaultPoolTask(execution, items, transactionStatuses, flag)));
        }
        try {
//            while (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
//                log.info("线程未结束");
//            }

            while (flag.getCompleteThreads().get() != flag.getGroupSize()) {
                Thread.sleep(100);
                log.info("等待子线程：getGroupSize:" + flag.getGroupSize() + "\tgetCompleteThreads：" + flag.getCompleteThreads().get());
                log.info("开启事务个数：" + transactionStatuses.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean completeSuccessIs = false;

//        log.info("没有出现任何异常,进行事务提交");
//        transactionStatuses.forEach(obj -> {
//
//            DefaultTransactionStatus status = (DefaultTransactionStatus) obj;
//            JdbcTransactionObjectSupport transaction = (JdbcTransactionObjectSupport) status.getTransaction();
//
//            if (obj.isCompleted()) {
//                log.info("事务已经提交");
//            } else {
//                log.info("主线程提交事务");
//
//                try {
//                    transaction.getConnectionHolder().getConnection().commit();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                    throw new FlowRuntimeException(ErrorCode.THREAD_POOL_EXECUTOR_SPRING_TX_EXCEPTION, "批处理异常：" + e.getMessage());
//                }
//            }
//        });

        try {
            for (Future<?> future : futures) {
                future.get();
            }
            completeSuccessIs = true;
        } catch (Exception e) {
            e.printStackTrace();

//            log.error("子线程出现任何异常,进行事务回滚");
//            transactionStatuses.forEach(obj -> {
//
//                DefaultTransactionStatus status = (DefaultTransactionStatus) obj;
//                JdbcTransactionObjectSupport transaction = (JdbcTransactionObjectSupport) status.getTransaction();
//                try {
//                    transaction.getConnectionHolder().getConnection().rollback();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                    throw new FlowRuntimeException(ErrorCode.THREAD_POOL_EXECUTOR_SPRING_TX_EXCEPTION, "批处理异常：" + e.getMessage());
//                }
//            });


            if (e.getCause() instanceof FlowableException) {
                Throwable exception = e.getCause();
                if (exception.getCause() instanceof PropertyNotFoundException) {
                    throw new FlowRuntimeException(ErrorCode.FLOW_EL_PROPERTY_NOT_FOUND_EXCEPTION, "批处理异常：" + exception.getMessage());
                }
                throw new FlowRuntimeException(ErrorCode.FLOW_EXCEPTION, "批处理异常：" + exception.getMessage());
            }
            throw new FlowRuntimeException(ErrorCode.FLOW_BATCH_EXCEPTION, "批处理异常：" + e.getMessage());
        } finally {
            executorService.shutdown();

            for (TransactionStatus transactionStatus : transactionStatuses) {
                DefaultTransactionStatus status = (DefaultTransactionStatus) transactionStatus;
                JdbcTransactionObjectSupport transaction = (JdbcTransactionObjectSupport) status.getTransaction();

                try {
                    if (completeSuccessIs){
                        transaction.getConnectionHolder().getConnection().commit();
                    }else {
                        transaction.getConnectionHolder().getConnection().rollback();
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new FlowRuntimeException(ErrorCode.THREAD_POOL_EXECUTOR_SPRING_TX_EXCEPTION, "批处理异常：" + e.getMessage());
                }
                transaction.getConnectionHolder().clear();
            }

        }


        return true;
    }

}
