package com.example.oldguy.examples.trainstactions;

import com.example.oldguy.modules.app.dao.entities.ActTaskEntity;
import com.example.oldguy.modules.app.dao.entities.TaskActionLog;
import com.example.oldguy.modules.app.dao.jpas.TaskActionLogMapper;
import com.example.oldguy.modules.app.services.TaskActionLogService;
import org.flowable.task.api.TaskInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author: oldguy
 * @date: 2020/11/6 11:52
 * @description:
 **/
@SpringBootTest
public class SpringTransactionTests {

//    @Autowired
//    private TransactionManager transactionManager;

    @Resource
    private TaskActionLogService taskActionLogService;

    @Autowired
    private TaskActionLogMapper taskActionLogMapper;

    @Test
    @Transactional(rollbackFor = Exception.class)
    public void test(){
        TaskActionLog log = new TaskActionLog();
        log.setType(TaskActionLog.taskActionType.COMPLETE_TASK.ordinal());
        log.setTaskName("测试-"+System.currentTimeMillis());
        taskActionLogMapper.insert(log);

        System.out.println("test");
    }

    @Test
    public void testPersist(){

        ActTaskEntity info = new ActTaskEntity();
        info.setId("test-001");

        taskActionLogService.persist(info,TaskActionLog.taskActionType.COMPLETE_TASK,"");
        System.out.println("测试");
        taskActionLogService.persist(info,TaskActionLog.taskActionType.COMPLETE_TASK,"");

    }

}
