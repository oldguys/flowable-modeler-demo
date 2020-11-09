package com.example.oldguy.modules.flow.services.batchs;

import java.util.List;

/**
 * @ClassName: ThreadExecution
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/28 0028 上午 10:37
 * @Version：
 **/
public interface ThreadExecution {

    /**
     *  处理线程任务
     * @param list
     */
    void threadExecute(List<?> list);
}
