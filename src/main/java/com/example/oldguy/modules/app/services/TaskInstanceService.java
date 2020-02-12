package com.example.oldguy.modules.app.services;

import com.example.oldguy.modules.app.dto.rsp.TaskRsp;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.service.impl.TaskQueryProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: TaskInstanceService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/11 0011 下午 9:13
 * @Version：
 **/
@Service
public class TaskInstanceService {


    @Autowired
    private TaskService taskService;

    /**
     *  获取任务列表
     * @param assignee
     * @return
     */
    public List<TaskRsp> getTaskList(String assignee) {


        TaskQuery taskQuery = taskService.createTaskQuery();

        if (!StringUtils.isEmpty(assignee)) {
            taskQuery.taskCandidateOrAssigned(assignee);
        }

        List<Task> taskList = taskQuery.orderBy(TaskQueryProperty.CREATE_TIME).desc().list();
        List<TaskRsp> records = new ArrayList<>(taskList.size());

        taskList.stream().forEach(obj -> {

            List<String> assignees = new ArrayList<>();
            assignees.add(obj.getAssignee());
            records.add(new TaskRsp(
                    obj.getId(),
                    obj.getProcessInstanceId(),
                    obj.getProcessDefinitionId(),
                    assignees));
        });

        return records;
    }

}
