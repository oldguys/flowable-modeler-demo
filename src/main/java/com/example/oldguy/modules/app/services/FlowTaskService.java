package com.example.oldguy.modules.app.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.oldguy.modules.app.dao.entities.ActTaskEntity;
import com.example.oldguy.modules.app.dao.entities.TaskActionLog;
import com.example.oldguy.modules.app.dao.jpas.ActTaskEntityMapper;
import com.example.oldguy.modules.examples.dto.batchs.BatchCompleteTaskItem;
import org.flowable.engine.TaskService;
import org.flowable.task.api.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName: FlowTaskService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/3 0003 下午 5:41
 * @Version：
 **/
@Service
public class FlowTaskService {

    @Autowired
    private TaskActionLogService logService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActTaskEntityMapper actTaskEntityMapper;

    @Transactional(rollbackFor = Exception.class)
    public void completeTask(List<BatchCompleteTaskItem> itemList) {

        Map<String, Map<String, Object>> taskDataMap = itemList
                .stream()
                .collect(Collectors.toMap(BatchCompleteTaskItem::getTaskId, BatchCompleteTaskItem::getData));

        QueryWrapper<ActTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("ID_", taskDataMap.keySet());
        List<ActTaskEntity> taskList = actTaskEntityMapper.selectList(queryWrapper);

        taskList.forEach(obj -> {
            Map<String, Object> data = taskDataMap.get(obj.getId());
            taskService.complete(obj.getId(), data);
            logService.persist(obj, TaskActionLog.taskActionType.COMPLETE_TASK, null);
        });
    }
}
