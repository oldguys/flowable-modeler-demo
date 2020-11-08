package com.example.oldguy.services;

import com.example.oldguy.modules.app.dao.entities.ActTaskEntity;
import com.example.oldguy.modules.app.dao.entities.TaskActionLog;
import com.example.oldguy.modules.app.services.TaskActionLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;


/**
 * @author: oldguy
 * @date: 2020/11/6 15:46
 * @description:
 **/
@SpringBootTest
public class TaskActionLogServiceTests {

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Autowired
    private TaskActionLogService taskActionLogService;

    @Test
    public void test() {


        TransactionStatus transactionStatus = transactionManager.getTransaction(TransactionDefinition.withDefaults());

        ActTaskEntity task = new ActTaskEntity();
        task.setId("12345");
        task.setName("测试-12345");

        taskActionLogService.persist(task, TaskActionLog.taskActionType.COMPLETE_TASK, "");
        System.out.println("完成持久化");

//        transactionManager.commit(transactionStatus);
        transactionManager.rollback(transactionStatus);
    }
}
