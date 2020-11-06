package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.modules.flow.exceptions.FlowRuntimeException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: oldguy
 * @date: 2020/11/6 12:17
 * @description:
 **/
@Data
@Slf4j
public class BatchTransactionFlag {

    /**
     * 总数
     */
    private int allGroupSize;

    /**
     * 成功事务数
     */
    private AtomicInteger successThread = new AtomicInteger();

    /**
     * thread id 集合
     */
    private List<UUID> errorThreadIds = new Vector<>();

    public BatchTransactionFlag(int allGroup) {
        this.allGroupSize = allGroup;
    }

    public void end() {
        while (allGroupSize != successThread.get()) {
            try {
                Thread.sleep(50);
                log.info("等待子线程处理");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        notifyAll();
        //统计失败的线程个数
        if (!errorThreadIds.isEmpty()) {
            throw new FlowRuntimeException("出现异常");
        }
    }

    /**
     * 等待全部结束
     */
    public synchronized void waitForEnd() {
        //统计失败的线程个数
        successThread.incrementAndGet();
        while (allGroupSize != successThread.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.info("测试异常：waitForEnd");
                e.printStackTrace();
            }
        }
    }

}
