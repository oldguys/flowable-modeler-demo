package com.example.oldguy.modules.flow.services.batchs.commons;

import lombok.Getter;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: oldguy
 * @date: 2020/11/6 17:21
 * @description:
 **/
@Getter
public class BatchTransactionFlag {

    private final AtomicInteger completeThreads = new AtomicInteger();

    private final AtomicInteger successThreads = new AtomicInteger();

    private final int groupSize;

    private boolean batchTransaction;

    private Map<Long, TransactionStatus> longTransactionStatusMap;

    private final List<?> toDoList;

    public BatchTransactionFlag(int groupSize, boolean batchTransaction, List<?> toDoList) {
        this.groupSize = groupSize;
        this.batchTransaction = batchTransaction;
        this.toDoList = toDoList;
        if (batchTransaction) {
            longTransactionStatusMap = new ConcurrentHashMap<>();
        }
    }
}
