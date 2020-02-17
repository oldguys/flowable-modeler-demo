package com.example.oldguy.modules.app.dto.rsp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: ProcessInstanceRsp
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/15 0015 上午 8:33
 * @Version：
 **/
@Data
public class ProcessInstanceRsp {

    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

    @ApiModelProperty("流程定义ID")
    private String processDefinitionId;

    @ApiModelProperty("流程定义名称")
    private String processDefinitionName;
}
