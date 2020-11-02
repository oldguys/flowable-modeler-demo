package com.example.oldguy.modules.examples.dto.batchs.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: BatchStartProcessInstanceReq
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/10/30 0030 下午 2:34
 * @Version：
 **/
@Data
public class BatchStartProcessInstanceReq {

    @NotEmpty(message = "itemList 不能为空！")
    private List<StartProcessItem> itemList = Collections.emptyList();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StartProcessItem{

        @NotBlank(message = "key 不能为空！")
        private String key;

        @NotEmpty(message = "sequence 不能为空！")
        private List<String> sequence = Collections.emptyList();

        private Map<String, Object> data = Collections.emptyMap();
    }
}
