package com.example.oldguy.modules.examples.dto.batchs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @ClassName: BatchCompleteTaskItem
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 5:12
 * @Version：
 **/
@Data
@AllArgsConstructor
public class BatchCompleteTaskItem {

    private String taskId;

    private String comment;

    private Map<String, Object> data;
}
