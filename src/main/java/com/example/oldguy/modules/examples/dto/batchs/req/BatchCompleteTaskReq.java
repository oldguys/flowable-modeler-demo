package com.example.oldguy.modules.examples.dto.batchs.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: BatchCompleteTaskReq
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 5:04
 * @Version：
 **/
@Data
public class BatchCompleteTaskReq {

    @NotEmpty(message = "itemList 不能为空！")
    private List<BatchCompleteTaskReq.CompleteTaskItem> itemList = new ArrayList<>();

    @ApiModelProperty("是否进行批量事务")
    private Boolean transactionalBatchIs = true;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompleteTaskItem {

        @NotEmpty(message = "taskIds 不能为空！")
        private List<String> taskIds = Collections.emptyList();

        private String comment;

        private Map<String, Object> data = Collections.emptyMap();
    }
}
