package com.example.oldguy.modules.flow.services.batchs.commons;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: oldguy
 * @date: 2020/11/6 17:21
 * @description:
 **/
@Getter
public class BatchTransactionFlag {

    private final AtomicInteger completeThreads = new AtomicInteger();

    private final int groupSize;

    public BatchTransactionFlag(int groupSize) {
        this.groupSize = groupSize;
    }
}
