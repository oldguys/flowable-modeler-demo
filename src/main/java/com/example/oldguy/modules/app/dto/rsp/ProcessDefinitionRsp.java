package com.example.oldguy.modules.app.dto.rsp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ProcessDefinitionRsp
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/11 0011 下午 7:23
 * @Version：
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDefinitionRsp {

    @ApiModelProperty("流程定义ID")
    private String processDefinitionId;

    @ApiModelProperty("流程定义名称")
    private String processDefinitionName;

    @ApiModelProperty("部署ID")
    private String developId;
}
