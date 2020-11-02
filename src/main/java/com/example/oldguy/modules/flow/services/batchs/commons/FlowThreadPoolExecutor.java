package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.common.dto.ErrorCode;
import com.example.oldguy.modules.flow.exceptions.FlowRuntimeException;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.javax.el.PropertyNotFoundException;
import org.flowable.ui.common.security.SecurityUtils;

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

    public static boolean executeTask(ThreadExecution execution, List toDoList, int groupSize) {
        int corePoolSize = 5;
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

        ExecutorService executorService = new ThreadPoolExecutor(
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


        List<Future> futures = new ArrayList<>();

        int group = toDoList.size() / groupSize;
        if (toDoList.size() % groupSize > 0) {
            group += 1;
        }
        for (int i = 0; i < group; i++) {

            int startIndex = i * groupSize;
            int endIndex = (i + 1) * groupSize;
            if (endIndex > toDoList.size()) {
                endIndex = toDoList.size();
            }
            List items = toDoList.subList(startIndex, endIndex);

            futures.add(executorService.submit(new DefaultPoolTask(execution, items)));
        }

        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getCause() instanceof FlowableException) {
                    Throwable exception = e.getCause();
                    if(exception.getCause() instanceof PropertyNotFoundException){
                        throw new FlowRuntimeException(ErrorCode.FLOW_EL_PROPERTY_NOT_FOUND_EXCEPTION, "批处理异常：" + exception.getMessage());
                    }
                    throw new FlowRuntimeException(ErrorCode.FLOW_EXCEPTION, "批处理异常：" + exception.getMessage());
                }
                throw new FlowRuntimeException(ErrorCode.FLOW_BATCH_EXCEPTION, "批处理异常：" + e.getMessage());
            }
        }

        return true;
    }

}
