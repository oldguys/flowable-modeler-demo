package com.example.oldguy.modules.app.services;

import com.example.oldguy.modules.app.dao.entities.TaskActionLog;
import com.example.oldguy.modules.app.dao.jpas.TaskActionLogMapper;
import org.flowable.task.api.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName: TaskActionLogService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 5:49
 * @Version：
 **/
@Service
public class TaskActionLogService {

    @Autowired
    private TaskActionLogMapper taskActionLogMapper;

    @Transactional(rollbackFor = Exception.class)
    public void persist(TaskInfo taskInfo, TaskActionLog.taskActionType type, String commentId) {
        TaskActionLog log = new TaskActionLog(taskInfo, type, commentId);
        taskActionLogMapper.insert(log);
    }
}
