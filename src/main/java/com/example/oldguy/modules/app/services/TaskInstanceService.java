package com.example.oldguy.modules.app.services;

import com.example.oldguy.modules.app.dto.rsp.TaskRsp;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
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
    @Autowired
    private HistoryService historyService;

    public List<TaskRsp>  getHistoryListByProcessInstanceId(String processInstanceId) {

        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .list();

        List<TaskRsp> records = new ArrayList<>();

        taskList.stream().forEach(obj -> {
            records.add(trainToTaskRsp(obj));
        });
        return records;
    }

    public List<TaskRsp> getTaskListByProcessInstanceId(String processInstanceId) {

        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();

        List<TaskRsp> records = new ArrayList<>();

        taskList.stream().forEach(obj -> {
            records.add(trainToTaskRsp(obj));
        });
        return records;
    }

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
            records.add(trainToTaskRsp(obj));
        });

        return records;
    }

    public TaskRsp trainToTaskRsp(TaskInfo task){
        List<String> assignees = new ArrayList<>();
        assignees.add(task.getAssignee());
//        TaskRsp rsp = new TaskRsp(task.getId(), task.getProcessInstanceId(), task.getProcessDefinitionId(), assignees);
        TaskRsp rsp = new TaskRsp(task, assignees);
        return rsp;
    }


}
