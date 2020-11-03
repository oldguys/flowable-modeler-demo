package com.example.oldguy.modules.examples.dto.batchs.rsp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: BatchStartProcessInstanceRsp
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/30 0030 下午 3:31
 * @Version：
 **/
@Data
@AllArgsConstructor
public class BatchCompleteTaskRsp {

    private List<BatchCompleteTaskRsp.CompleteTaskItem> itemList;

    @Data
    @AllArgsConstructor
    public static class CompleteTaskItem {

        @ApiModelProperty("流程实例ID")
        private String processInstanceId;

        @ApiModelProperty("任务ID")
        private String taskId;
    }
}
