package com.example.oldguy.modules.examples.dto.batchs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @ClassName: BatchStartProcessInstanceItem
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/30 0030 下午 3:09
 * @Version：
 **/
@Data
@AllArgsConstructor
public class BatchStartProcessInstanceItem {

    private String sequenceNo;

    private String key;

    private Map<String, Object> data;
}
