package com.example.oldguy.modules.examples.dto.batchs.rsp;

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
public class BatchStartProcessInstanceRsp {

    private List<ProcessInstanceItem> itemList;

    @Data
    @AllArgsConstructor
    public static class ProcessInstanceItem {

        private String sequenceNo;

        private String processInstanceId;
    }
}
