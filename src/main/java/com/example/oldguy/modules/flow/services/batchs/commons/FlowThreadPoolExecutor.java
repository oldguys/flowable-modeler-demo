package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.common.dto.ErrorCode;
import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.exceptions.FlowRuntimeException;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.javax.el.PropertyNotFoundException;
import org.flowable.ui.common.security.SecurityUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
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

    public static boolean executeTask(ThreadExecution execution, List toDoList, int groupSize, boolean batchTransaction) {
        int corePoolSize = 10;
        int maximumPoolSize = 10;
        long keepAliveTime = 3;
        return executeTask(corePoolSize, maximumPoolSize, keepAliveTime, execution, toDoList, groupSize, batchTransaction);
    }

    public static boolean executeTask(int corePoolSize,
                                      int maximumPoolSize,
                                      long keepAliveTime,
                                      ThreadExecution execution,
                                      List<?> toDoList,
                                      int groupSize,
                                      boolean batchTransaction
    ) {
        if (toDoList.isEmpty()) {
            log.warn("空任务组,不需要进行处理");
            return false;
        }

        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

        int group;

        if (batchTransaction) {
            if (toDoList.size() <= corePoolSize) {
                group = 1;
                groupSize = toDoList.size();
            } else {
                groupSize = toDoList.size() / corePoolSize;
                if (toDoList.size() % corePoolSize > 1) {
                    corePoolSize += 1;
                    maximumPoolSize += 1;
                }
                group = corePoolSize;
            }
        } else {
            group = toDoList.size() / groupSize;
            if (toDoList.size() % groupSize > 0) {
                group += 1;
            }
        }


        // 事务集合
        BatchTransactionFlag flag = new BatchTransactionFlag(group, batchTransaction, toDoList);

        ThreadPoolExecutor executorService = createThreadPoolExecutorInstance(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                flag);


        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < group; i++) {

            int startIndex = i * groupSize;
            int endIndex = (i + 1) * groupSize;
            if (endIndex > toDoList.size()) {
                endIndex = toDoList.size();
            }
            List<?> items = toDoList.subList(startIndex, endIndex);
            if (batchTransaction) {
                futures.add(executorService.submit(new BatchTransactionPoolTask(execution, items, flag.getLongTransactionStatusMap(), flag)));
            } else {
                futures.add(executorService.submit(new SingleTransactionPoolTask(execution, items, flag)));
            }
        }
        try {

            while (flag.getCompleteThreads().get() != flag.getGroupSize()) {
                Thread.sleep(1000);
                log.info("等待子线程：getGroupSize:" + flag.getGroupSize() + "\t getCompleteThreads：" + flag.getCompleteThreads().get() + "\t pageSize:" + groupSize);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();

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

        }

        return true;
    }

    private static ThreadPoolExecutor createThreadPoolExecutorInstance(int corePoolSize,
                                                                       int maximumPoolSize,
                                                                       long keepAliveTime,
                                                                       TimeUnit unit,
                                                                       BlockingQueue<Runnable> workQueue,
                                                                       BatchTransactionFlag flag
    ) {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("flow-pool-%d")
                .build();

        String currentUserId = SecurityUtils.getCurrentUserId();
        DataSourceTransactionManager transactionManager = SpringContextUtils.getBean(DataSourceTransactionManager.class);


        return new ThreadPoolExecutor(
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

            @Override
            protected void afterExecute(Runnable r, Throwable t) {

                if (flag.isBatchTransaction()) {

                    try {
                        while (flag.getCompleteThreads().get() != flag.getGroupSize()) {
                            log.info(Thread.currentThread().getName() + " 等待主线程：getGroupSize:" + flag.getGroupSize() + "\tgetCompleteThreads：" + flag.getCompleteThreads().get());
                            log.info("开启事务个数：" + flag.getLongTransactionStatusMap().size());
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    TransactionStatus status = flag.getLongTransactionStatusMap().get(Thread.currentThread().getId());
                    if (flag.getSuccessThreads().get() == flag.getCompleteThreads().get()) {
                        log.info(Thread.currentThread().getName() + ":全部执行成功,提交事务");
                        transactionManager.commit(status);
                    } else {
                        log.info(Thread.currentThread().getName() + ":具有线程执行失败,回滚事务");
                        transactionManager.rollback(status);
                    }
                }
            }
        };
    }

}
