package com.example.oldguy.modules.app.dto.rsp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @ClassName: TaskRsp
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/11 0011 下午 9:14
 * @Version：
 **/
@Data
@NoArgsConstructor
public class TaskRsp {

    @ApiModelProperty("任务ID")
    private String taskId;

    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

//    @ApiModelProperty("流程定义Key")
//    private String processDefinitionKey;

    @ApiModelProperty("流程定义ID")
    private String processDefinitionId;

    @ApiModelProperty("任务执行人集合")
    private List<String> assignees = Collections.emptyList();

    public TaskRsp(String taskId, String processInstanceId, String processDefinitionId, List<String> assignees) {
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
        this.processDefinitionId = processDefinitionId;
        this.assignees = assignees;
    }
}
