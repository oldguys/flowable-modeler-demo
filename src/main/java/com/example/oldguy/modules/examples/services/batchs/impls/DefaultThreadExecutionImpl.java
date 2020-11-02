package com.example.oldguy.modules.examples.services.batchs.impls;

import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @ClassName: DefaultThreadExecutionImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/29 0029 下午 2:47
 * @Version：
 **/
@Slf4j
public class DefaultThreadExecutionImpl implements ThreadExecution {

    private Map<String, List<String>> map;

    public DefaultThreadExecutionImpl(Map<String, List<String>> map) {
        this.map = map;
    }

    @Override
    public void threadExecute(List list) {

        String threadName = Thread.currentThread().getName();
        log.info("当前线程：" + threadName);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<String> records = list;
        records.forEach(obj -> map.computeIfAbsent(threadName, item -> new Vector<>()).add(obj));
    }
}
