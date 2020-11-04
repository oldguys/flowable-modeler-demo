package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.flow.services.batchs.CommonThreadExecutionService;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName: DefaultPoolTask
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/29 0029 下午 2:44
 * @Version：
 **/
public class DefaultPoolTask implements Runnable {

    private ThreadExecution threadExecution;

    private List list;

    private CommonThreadExecutionService commonThreadExecutionService;

    public DefaultPoolTask(ThreadExecution threadExecution, List list) {
        this.threadExecution = threadExecution;
        this.list = list;
        this.commonThreadExecutionService = SpringContextUtils.getBean(CommonThreadExecutionService.class);
    }

    @Override
    public void run() {
        commonThreadExecutionService.executeBatch(threadExecution, list);
    }
}
